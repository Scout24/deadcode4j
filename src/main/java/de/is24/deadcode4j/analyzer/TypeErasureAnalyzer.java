package de.is24.deadcode4j.analyzer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor;
import de.is24.javaparser.FixedVoidVisitorAdapter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import japa.parser.JavaParser;
import japa.parser.TokenMgrError;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.Node;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.type.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.*;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static de.is24.javaparser.ImportDeclarations.isAsterisk;
import static de.is24.javaparser.ImportDeclarations.isStatic;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Analyzes Java files and reports dependencies to classes that are not part of the byte code due to type erasure.
 * <b>Note</b> that references to "inherited" types are not analyzed, as they are found by the byte code analysis.
 *
 * @since 1.6
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class TypeErasureAnalyzer extends AnalyzerAdapter {

    @Nonnull
    private static ClassOrInterfaceType getFirstNode(@Nonnull ClassOrInterfaceType fieldAccessExpr) {
        ClassOrInterfaceType scope = fieldAccessExpr.getScope();
        return scope == null ? fieldAccessExpr : getFirstNode(scope);
    }

    @Nonnull
    private static String getQualifier(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
        StringBuilder buffy = new StringBuilder(classOrInterfaceType.getName());
        while ((classOrInterfaceType = classOrInterfaceType.getScope()) != null) {
            buffy.insert(0, '.');
            buffy.insert(0, classOrInterfaceType.getName());
        }
        return buffy.toString();
    }

    @Nonnull
    private static String getTypeName(@Nonnull Node node) {
        StringBuilder buffy = new StringBuilder();
        for (; ; ) {
            if (TypeDeclaration.class.isInstance(node)) {
                if (buffy.length() > 0) {
                    buffy.insert(0, '$');
                }
                buffy.insert(0, TypeDeclaration.class.cast(node).getName());
            } else if (CompilationUnit.class.isInstance(node)) {
                final CompilationUnit compilationUnit = CompilationUnit.class.cast(node);
                if (compilationUnit.getPackage() != null) {
                    prepend(compilationUnit.getPackage().getName(), buffy);
                }
            }
            node = node.getParentNode();
            //noinspection ConstantConditions
            if (node == null) {
                break;
            }
        }
        return buffy.toString();
    }

    @Nonnull
    private static StringBuilder prepend(@Nonnull NameExpr nameExpr, @Nonnull StringBuilder buffy) {
        for (; ; ) {
            if (buffy.length() > 0) {
                buffy.insert(0, '.');
            }
            buffy.insert(0, nameExpr.getName());
            if (!QualifiedNameExpr.class.isInstance(nameExpr)) {
                break;
            }
            nameExpr = QualifiedNameExpr.class.cast(nameExpr).getQualifier();
        }
        return buffy;
    }

    @Override
    @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "The MavenProject does not provide the proper encoding")
    public void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File file) {
        if (file.getName().endsWith(".java")) {
            logger.debug("Analyzing Java file [{}]...", file);
            final CompilationUnit compilationUnit;
            Reader reader = null;
            try {
                reader = analysisContext.getModule().getEncoding() != null
                        ? new InputStreamReader(new FileInputStream(file), analysisContext.getModule().getEncoding())
                        : new FileReader(file);
                compilationUnit = JavaParser.parse(reader, false);
            } catch (TokenMgrError e) {
                throw new RuntimeException("Failed to parse [" + file + "]!", e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse [" + file + "]!", e);
            } finally {
                closeQuietly(reader);
            }
            analyzeCompilationUnit(analysisContext, compilationUnit);
        }
    }

    private void analyzeCompilationUnit(@Nonnull final AnalysisContext analysisContext, @Nonnull final CompilationUnit compilationUnit) {
        compilationUnit.accept(new FixedVoidVisitorAdapter<Void>() {
            private final ClassPoolAccessor classPoolAccessor = classPoolAccessorFor(analysisContext);
            private final Deque<Set<String>> definedTypeParameters = newLinkedList();
            private final Map<String, Set<String>> processedReferences = newHashMap();

            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
                try {
                    super.visit(n, arg);
                } finally {
                    this.definedTypeParameters.removeLast();
                }
            }

            @Override
            public void visit(ConstructorDeclaration n, Void arg) {
                this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
                try {
                    super.visit(n, arg);
                } finally {
                    this.definedTypeParameters.removeLast();
                }
            }

            @Override
            public void visit(MethodDeclaration n, Void arg) {
                this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
                try {
                    super.visit(n, arg);
                } finally {
                    this.definedTypeParameters.removeLast();
                }
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

            private boolean typeParameterWithSameNameIsDefined(@Nonnull ClassOrInterfaceType nestedClassOrInterface) {
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

            private boolean typeReferenceHasAlreadyBeenProcessed(ClassOrInterfaceType referencedType) {
                Set<String> references = this.processedReferences.get(getTypeName(referencedType));
                return references != null && references.contains(getQualifier(referencedType));
            }

            private void resolveTypeReference(ClassOrInterfaceType referencedType) {
                @SuppressWarnings("unchecked")
                Optional<String> resolvedClass = or(
                        resolveFullyQualifiedClass(),
                        resolveInnerType(),
                        resolveImport(),
                        resolvePackageType(),
                        resolveAsteriskImports(),
                        resolveJavaLangType()
                ).apply(referencedType);
                assert resolvedClass != null;
                String depender = getTypeName(referencedType);
                String referencedTypeQualifier = getQualifier(referencedType);
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
                        return classPoolAccessor.resolveClass(getQualifier(typeReference));
                    }
                };
            }

            @Nonnull
            private Function<ClassOrInterfaceType, Optional<String>> resolveInnerType() {
                return new Function<ClassOrInterfaceType, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull ClassOrInterfaceType typeReference) {
                        ClassOrInterfaceType firstQualifier = getFirstNode(typeReference);
                        Node loopNode = typeReference;
                        for (; ; ) {
                            if (TypeDeclaration.class.isInstance(loopNode)) {
                                TypeDeclaration typeDeclaration = TypeDeclaration.class.cast(loopNode);
                                Optional<String> reference = resolveInnerReference(firstQualifier, asList(typeDeclaration));
                                if (reference.isPresent()) {
                                    return reference;
                                }
                                reference = resolveInnerReference(firstQualifier, emptyIfNull(typeDeclaration.getMembers()));
                                if (reference.isPresent()) {
                                    return reference;
                                }
                            } else if (CompilationUnit.class.isInstance(loopNode)) {
                                Optional<String> reference = resolveInnerReference(firstQualifier, emptyIfNull(CompilationUnit.class.cast(loopNode).getTypes()));
                                if (reference.isPresent())
                                    return reference;
                            }
                            loopNode = loopNode.getParentNode();
                            if (loopNode == null) {
                                break;
                            }
                        }

                        return absent();
                    }
                };
            }

            private Optional<String> resolveInnerReference(@Nonnull ClassOrInterfaceType firstQualifier,
                                                           @Nonnull Iterable<? extends BodyDeclaration> bodyDeclarations) {
                for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
                    if (!TypeDeclaration.class.isInstance(bodyDeclaration)) {
                        continue;
                    }
                    final TypeDeclaration typeDeclaration = TypeDeclaration.class.cast(bodyDeclaration);
                    if (firstQualifier.getName().equals(typeDeclaration.getName())) {
                        return of(resolveReferencedType(firstQualifier, typeDeclaration));
                    }
                }
                return absent();
            }

            @Nonnull
            private String resolveReferencedType(@Nonnull ClassOrInterfaceType firstQualifier,
                                                 @Nonnull TypeDeclaration typeDeclaration) {
                if (ClassOrInterfaceType.class.isInstance(firstQualifier.getParentNode())) {
                    ClassOrInterfaceType nextQualifier = ClassOrInterfaceType.class.cast(firstQualifier.getParentNode());
                    if (nextQualifier.getScope() == firstQualifier) {
                        for (BodyDeclaration bodyDeclaration : emptyIfNull(typeDeclaration.getMembers())) {
                            if (!TypeDeclaration.class.isInstance(bodyDeclaration)) {
                                continue;
                            }
                            TypeDeclaration nextTypeDeclaration = TypeDeclaration.class.cast(bodyDeclaration);
                            if (nextQualifier.getName().equals(nextTypeDeclaration.getName())) {
                                return resolveReferencedType(nextQualifier, nextTypeDeclaration);
                            }
                        }
                    }
                }

                return getTypeName(typeDeclaration);
            }

            @Nonnull
            private Function<ClassOrInterfaceType, Optional<String>> resolveImport() {
                return new Function<ClassOrInterfaceType, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull ClassOrInterfaceType typeReference) {
                        for (ImportDeclaration importDeclaration :
                                filter(emptyIfNull(compilationUnit.getImports()), not(isAsterisk()))) {
                            String importedClass = importDeclaration.getName().getName();
                            String typeReferenceQualifier = getQualifier(typeReference);
                            if (importedClass.equals(typeReferenceQualifier)
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
                        StringBuilder buffy = new StringBuilder(getQualifier(typeReference));
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
                                filter(emptyIfNull(compilationUnit.getImports()), and(isAsterisk(), not(isStatic())))) {
                            StringBuilder buffy = new StringBuilder(getQualifier(typeReference));
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
                        return classPoolAccessor.resolveClass("java.lang." + getQualifier(typeReference));
                    }
                };
            }

            @Nonnull
            private StringBuilder prependPackageName(@Nonnull StringBuilder buffy) {
                if (compilationUnit.getPackage() == null) {
                    return buffy;
                }
                return prepend(compilationUnit.getPackage().getName(), buffy);
            }


        }, null);
    }

}
