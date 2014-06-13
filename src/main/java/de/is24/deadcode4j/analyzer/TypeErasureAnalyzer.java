package de.is24.deadcode4j.analyzer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor;
import de.is24.javaparser.FixedVoidVisitorAdapter;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.Node;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.*;
import japa.parser.ast.type.*;
import javassist.CtClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.*;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static de.is24.deadcode4j.analyzer.javassist.CtClasses.*;
import static de.is24.javaparser.ImportDeclarations.isAsterisk;
import static de.is24.javaparser.ImportDeclarations.isStatic;
import static de.is24.javaparser.Nodes.getTypeName;
import static de.is24.javaparser.Nodes.prepend;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * Analyzes Java files and reports dependencies to classes that are not part of the byte code due to type erasure.
 * <b>Note</b> that references to "inherited" types are not analyzed, as they are found by the byte code analysis.
 *
 * @since 1.6
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class TypeErasureAnalyzer extends JavaFileAnalyzer {

    @Nonnull
    private static ClassOrInterfaceType getFirstQualifier(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
        ClassOrInterfaceType scope = classOrInterfaceType.getScope();
        return scope == null ? classOrInterfaceType : getFirstQualifier(scope);
    }

    @Nonnull
    private static String getFullQualifier(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
        StringBuilder buffy = new StringBuilder(classOrInterfaceType.getName());
        while ((classOrInterfaceType = classOrInterfaceType.getScope()) != null) {
            buffy.insert(0, '.');
            buffy.insert(0, classOrInterfaceType.getName());
        }
        return buffy.toString();
    }

    @Nonnull
    private static ClassOrInterfaceType getLastQualifier(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
        ClassOrInterfaceType parentQualifier = getParentQualifier(classOrInterfaceType);
        return parentQualifier == null ? classOrInterfaceType : getLastQualifier(parentQualifier);
    }

    @Nullable
    private static ClassOrInterfaceType getParentQualifier(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
        Node parentNode = classOrInterfaceType.getParentNode();
        if (!ClassOrInterfaceType.class.isInstance(parentNode)) {
            return null;
        }
        ClassOrInterfaceType parent = ClassOrInterfaceType.class.cast(parentNode);
        return parent.getScope() == classOrInterfaceType ? parent : null;
    }

    @Override
    protected void analyzeCompilationUnit(@Nonnull final AnalysisContext analysisContext, @Nonnull final CompilationUnit compilationUnit) {
        compilationUnit.accept(new TypeParameterRecordingVisitor<Void>() {
            private final ClassPoolAccessor classPoolAccessor = classPoolAccessorFor(analysisContext);
            private final Map<String, Set<String>> processedReferences = newHashMap();

            @Override
            public void visit(ClassOrInterfaceType n, Void arg) {
                for (Type type : emptyIfNull(n.getTypeArgs())) {
                    ClassOrInterfaceType referencedType = getReferencedType(type);
                    if (referencedType == null) {
                        continue;
                    }
                    if (typeParameterWithSameNameIsDefined(referencedType)) {
                        continue;
                    }
                    if (typeReferenceHasAlreadyBeenProcessed(referencedType)) {
                        continue;
                    }
                    resolveTypeReference(referencedType);
                    this.visit(referencedType, arg); // resolve nested type arguments
                }
            }

            @Nullable
            private ClassOrInterfaceType getReferencedType(@Nonnull Type type) {
                final Type nestedType;
                if (ReferenceType.class.isInstance(type)) {
                    nestedType = ReferenceType.class.cast(type).getType();
                } else if (WildcardType.class.isInstance(type)) {
                    WildcardType wildcardType = WildcardType.class.cast(type);
                    ReferenceType referenceType = wildcardType.getExtends();
                    if (referenceType == null) {
                        referenceType = wildcardType.getSuper();
                    }
                    if (referenceType == null) {
                        // unbounded wildcard - nothing ro refer to
                        return null;
                    }
                    nestedType = referenceType.getType();
                } else {
                    logger.warn("Encountered unexpected Type [{}:{}]; please create an issue at https://github.com/ImmobilienScout24/deadcode4j.", type.getClass(), type);
                    return null;
                }
                if (PrimitiveType.class.isInstance(nestedType)) {
                    // references to primitives won't be reported
                    return null;
                }
                if (!ClassOrInterfaceType.class.isInstance(nestedType)) {
                    logger.warn("[{}:{}] is no ClassOrInterfaceType; please create an issue at https://github.com/ImmobilienScout24/deadcode4j.", type.getClass(), type);
                    return null;
                }
                return ClassOrInterfaceType.class.cast(nestedType);
            }

            private boolean typeReferenceHasAlreadyBeenProcessed(ClassOrInterfaceType referencedType) {
                Set<String> references = this.processedReferences.get(getTypeName(referencedType));
                return references != null && references.contains(getFullQualifier(referencedType));
            }

            private void resolveTypeReference(ClassOrInterfaceType referencedType) {
                @SuppressWarnings("unchecked")
                Optional<String> resolvedClass = or(
                        resolveFullyQualifiedClass(),
                        resolveInnerType(),
                        resolveInheritedType(),
                        resolveImport(),
                        resolvePackageType(),
                        resolveAsteriskImports(),
                        resolveJavaLangType()
                ).apply(referencedType);
                assert resolvedClass != null;
                String depender = getTypeName(referencedType);
                String referencedTypeQualifier = getFullQualifier(referencedType);
                if (resolvedClass.isPresent()) {
                    analysisContext.addDependencies(depender, resolvedClass.get());
                } else {
                    logger.debug("Could not resolve Type Argument [{}] used by [{}].", referencedTypeQualifier, depender);
                }
                getOrAddMappedSet(this.processedReferences, depender).add(referencedTypeQualifier);
            }

            @Nonnull
            private Function<ClassOrInterfaceType, Optional<String>> resolveFullyQualifiedClass() {
                return new Function<ClassOrInterfaceType, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull ClassOrInterfaceType typeReference) {
                        if (typeReference.getScope() == null) {
                            return absent();
                        }
                        return classPoolAccessor.resolveClass(getFullQualifier(typeReference));
                    }
                };
            }

            @Nonnull
            private Function<ClassOrInterfaceType, Optional<String>> resolveInnerType() {
                return new Function<ClassOrInterfaceType, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull ClassOrInterfaceType typeReference) {
                        ClassOrInterfaceType firstQualifier = getFirstQualifier(typeReference);
                        Node loopNode = typeReference;
                        for (; ; ) {
                            Optional<String> reference;
                            if (TypeDeclaration.class.isInstance(loopNode)) {
                                TypeDeclaration typeDeclaration = TypeDeclaration.class.cast(loopNode);
                                reference = resolveInnerReference(firstQualifier, singleton(typeDeclaration));
                                if (reference.isPresent()) {
                                    return reference;
                                }
                                reference = resolveInnerReference(firstQualifier, typeDeclaration.getMembers());
                                if (reference.isPresent()) {
                                    return reference;
                                }
                            } else if (CompilationUnit.class.isInstance(loopNode)) {
                                reference = resolveInnerReference(firstQualifier, CompilationUnit.class.cast(loopNode).getTypes());
                                if (reference.isPresent())
                                    return reference;
                            }
                            loopNode = loopNode.getParentNode();
                            if (loopNode == null) {
                                return absent();
                            }
                        }
                    }
                };
            }

            @Nonnull
            private Optional<String> resolveInnerReference(
                    @Nonnull ClassOrInterfaceType firstQualifier,
                    @Nullable Iterable<? extends BodyDeclaration> bodyDeclarations) {
                for (TypeDeclaration typeDeclaration : emptyIfNull(bodyDeclarations).filter(TypeDeclaration.class)) {
                    if (firstQualifier.getName().equals(typeDeclaration.getName())) {
                        return of(resolveReferencedType(firstQualifier, typeDeclaration));
                    }
                }
                return absent();
            }

            @Nonnull
            private String resolveReferencedType(@Nonnull ClassOrInterfaceType qualifier,
                                                 @Nonnull TypeDeclaration type) {
                ClassOrInterfaceType parentQualifier = getParentQualifier(qualifier);
                if (parentQualifier != null) {
                    for (TypeDeclaration innerType : emptyIfNull(type.getMembers()).filter(TypeDeclaration.class)) {
                        if (parentQualifier.getName().equals(innerType.getName())) {
                            return resolveReferencedType(parentQualifier, innerType);
                        }
                    }
                }

                return getTypeName(type);
            }

            @Nonnull
            private Function<ClassOrInterfaceType, Optional<String>> resolveInheritedType() {
                return new Function<ClassOrInterfaceType, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull ClassOrInterfaceType typeReference) {
                        String typeName = getTypeName(typeReference);
                        CtClass clazz = getCtClass(classPoolAccessor.getClassPool(), typeName);
                        if (clazz == null) {
                            return absent();
                        }
                        ClassOrInterfaceType firstQualifier = getFirstQualifier(typeReference);
                        for (CtClass declaringClazz : getDeclaringClassesOf(clazz)) {
                            Optional<String> inheritedType = resolveInheritedType(declaringClazz, firstQualifier);
                            if (inheritedType.isPresent()){
                                return inheritedType;
                            }
                        }
                        return absent();
                    }
                };
            }

            @Nonnull
            private Optional<String> resolveInheritedType(
                    @Nonnull CtClass clazz,
                    @Nonnull ClassOrInterfaceType firstQualifier) {
                Optional<String> result = checkNestedClasses(getSuperclassOf(clazz), firstQualifier);
                if (result.isPresent()) {
                    return result;
                }
                for (CtClass interfaceClazz : getInterfacesOf(clazz)) {
                    result = checkNestedClasses(interfaceClazz, firstQualifier);
                    if (result.isPresent()) {
                        return result;
                    }
                }
                return absent();
            }

            @Nonnull
            private Optional<String> checkNestedClasses(
                    @Nullable CtClass clazz,
                    @Nonnull ClassOrInterfaceType firstQualifier) {
                if (clazz == null || isJavaLangObject(clazz)) {
                    return absent();
                }
                for (CtClass nestedClass : getNestedClassesOf(clazz)) {
                    String simpleName = nestedClass.getSimpleName();
                    simpleName = simpleName.substring(simpleName.lastIndexOf('$') + 1);
                    if (firstQualifier.getName().equals(simpleName)) {
                        return of(nestedClass.getName().substring(0, nestedClass.getName().length() - simpleName.length())
                                + getFullQualifier(getLastQualifier(firstQualifier)).replace('.', '$'));
                    }
                }
                return resolveInheritedType(clazz, firstQualifier);
            }

            @Nonnull
            private Function<ClassOrInterfaceType, Optional<String>> resolveImport() {
                return new Function<ClassOrInterfaceType, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull ClassOrInterfaceType typeReference) {
                        for (ImportDeclaration importDeclaration :
                                emptyIfNull(compilationUnit.getImports()).filter(not(isAsterisk()))) {
                            String importedClass = importDeclaration.getName().getName();
                            String typeReferenceQualifier = getFullQualifier(typeReference);
                            if (typeReferenceQualifier.equals(importedClass)
                                    || typeReferenceQualifier.startsWith(importedClass + ".")) {
                                StringBuilder buffy = prepend(importDeclaration.getName(), new StringBuilder());
                                buffy.append(typeReferenceQualifier.substring(importedClass.length()));
                                return classPoolAccessor.resolveClass(buffy);
                            }
                        }
                        return absent();
                    }
                };
            }

            @Nonnull
            private Function<ClassOrInterfaceType, Optional<String>> resolvePackageType() {
                return new Function<ClassOrInterfaceType, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull ClassOrInterfaceType typeReference) {
                        StringBuilder buffy = new StringBuilder(getFullQualifier(typeReference));
                        prependPackageName(buffy);
                        return classPoolAccessor.resolveClass(buffy);
                    }
                };
            }

            @Nonnull
            private Function<ClassOrInterfaceType, Optional<String>> resolveAsteriskImports() {
                return new Function<ClassOrInterfaceType, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull ClassOrInterfaceType typeReference) {
                        for (ImportDeclaration importDeclaration :
                                emptyIfNull(compilationUnit.getImports()).filter(and(isAsterisk(), not(isStatic())))) {
                            StringBuilder buffy = new StringBuilder(getFullQualifier(typeReference));
                            prepend(importDeclaration.getName(), buffy);
                            Optional<String> resolvedClass = classPoolAccessor.resolveClass(buffy);
                            if (resolvedClass.isPresent()) {
                                return resolvedClass;
                            }
                        }
                        return absent();
                    }
                };
            }

            @Nonnull
            private Function<ClassOrInterfaceType, Optional<String>> resolveJavaLangType() {
                return new Function<ClassOrInterfaceType, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull ClassOrInterfaceType typeReference) {
                        return classPoolAccessor.resolveClass("java.lang." + getFullQualifier(typeReference));
                    }
                };
            }

            @Nonnull
            private StringBuilder prependPackageName(@Nonnull StringBuilder buffy) {
                return compilationUnit.getPackage() == null
                        ? buffy
                        : prepend(compilationUnit.getPackage().getName(), buffy);
            }

        }, null);
    }

    private static class TypeParameterRecordingVisitor<A> extends FixedVoidVisitorAdapter<A> {
        private final Deque<Set<String>> definedTypeParameters = newLinkedList();

        @Override
        public void visit(ClassOrInterfaceDeclaration n, A arg) {
            this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
            try {
                super.visit(n, arg);
            } finally {
                this.definedTypeParameters.removeLast();
            }
        }

        @Override
        public void visit(ConstructorDeclaration n, A arg) {
            this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
            try {
                super.visit(n, arg);
            } finally {
                this.definedTypeParameters.removeLast();
            }
        }

        @Override
        public void visit(MethodDeclaration n, A arg) {
            this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
            try {
                super.visit(n, arg);
            } finally {
                this.definedTypeParameters.removeLast();
            }
        }

        protected boolean typeParameterWithSameNameIsDefined(@Nonnull ClassOrInterfaceType nestedClassOrInterface) {
            if (nestedClassOrInterface.getScope() != null) {
                return false;
            }
            for (Set<String> definedTypeNames : this.definedTypeParameters) {
                if (definedTypeNames.contains(nestedClassOrInterface.getName())) {
                    return true;
                }
            }
            return false;
        }

        @Nonnull
        private Set<String> getTypeParameterNames(@Nullable List<TypeParameter> typeParameters) {
            if (typeParameters == null)
                return emptySet();
            Set<String> parameters = newHashSet();
            for (TypeParameter typeParameter : typeParameters) {
                parameters.add(typeParameter.getName());
            }
            return parameters;
        }

    }

}
