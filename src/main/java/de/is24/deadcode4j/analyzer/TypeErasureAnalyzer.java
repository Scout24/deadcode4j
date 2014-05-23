package de.is24.deadcode4j.analyzer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor;
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
import japa.parser.ast.visitor.VoidVisitorAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

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
import static java.util.Collections.emptySet;
import static java.util.Map.Entry;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Analyzes Java files and reports dependencies to classes that are not part of the byte code due to type erasure.
 * <b>Note</b> that references to "inherited" types are not analyzed, as they are found by the byte code analysis.
 *
 * @since 1.6
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class TypeErasureAnalyzer extends AnalyzerAdapter {

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
        compilationUnit.accept(new TypeRecordingVisitor() {
            private final ClassPoolAccessor classPoolAccessor = classPoolAccessorFor(analysisContext);
            private final Deque<Set<String>> definedTypeParameters = newLinkedList();
            private final Map<String, Set<String>> typeReferences = newHashMap();

            @Override
            public void visit(CompilationUnit n, Void arg) {
                super.visit(n, arg);
                resolveTypeReferences();
            }

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
                    getOrAddMappedSet(this.typeReferences, getQualifier(referencedType)).add(getTypeName(n));
                    this.visit(referencedType, arg); // resolve nested type arguments
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

            @Nonnull
            private String getTypeName(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                StringBuilder buffy = new StringBuilder();
                Node node = classOrInterfaceType;
                while ((node = node.getParentNode()) != null) {
                    if (!TypeDeclaration.class.isInstance(node)) {
                        continue;
                    }
                    if (buffy.length() > 0)
                        buffy.insert(0, '$');
                    buffy.insert(0, TypeDeclaration.class.cast(node).getName());
                }
                return prependPackageName(buffy).toString();
            }

            @Nonnull
            private String getQualifier(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                StringBuilder buffy = new StringBuilder(classOrInterfaceType.getName());
                while ((classOrInterfaceType = classOrInterfaceType.getScope()) != null) {
                    buffy.insert(0, '.');
                    buffy.insert(0, classOrInterfaceType.getName());
                }
                return buffy.toString();
            }

            private void resolveTypeReferences() {
                for (Entry<String, Set<String>> typeReference : this.typeReferences.entrySet()) {
                    String referencedType = typeReference.getKey();
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
                    if (resolvedClass.isPresent()) {
                        for (String depender : typeReference.getValue()) {
                            analysisContext.addDependencies(depender, resolvedClass.get());
                        }
                    } else {
                        logger.debug("Could not resolve Type Argument [{}] used by [{}].", referencedType, typeReference.getValue());
                    }
                }
            }

            @Nonnull
            private Function<String, Optional<String>> resolveFullyQualifiedClass() {
                return new Function<String, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull String typeReference) {
                        if (typeReference.indexOf('.') < 0) {
                            return absent();
                        }
                        return classPoolAccessor.resolveClass(typeReference);
                    }
                };
            }

            @Nonnull
            private Function<String, Optional<String>> resolveInnerType() {
                return new Function<String, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull String typeReference) {
                        String potentialInnerType = getOuterMostType() + "$" + typeReference.replace('.', '$');
                        if (!getTypeNames().contains(potentialInnerType)) {
                            return absent();
                        }
                        StringBuilder buffy = new StringBuilder(potentialInnerType);
                        prependPackageName(buffy);
                        return of(buffy.toString());
                    }
                };
            }

            @Nonnull
            private Function<String, Optional<String>> resolveImport() {
                return new Function<String, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull String typeReference) {
                        for (ImportDeclaration importDeclaration :
                                filter(emptyIfNull(compilationUnit.getImports()), not(isAsterisk()))) {
                            String importedClass = importDeclaration.getName().getName();
                            if (importedClass.equals(typeReference) || typeReference.startsWith(importedClass + ".")) {
                                StringBuilder buffy = prepend(importDeclaration.getName(), new StringBuilder());
                                buffy.append(typeReference.substring(importedClass.length()));
                                return classPoolAccessor.resolveClass(buffy);
                            }
                        }
                        return absent();
                    }
                };
            }

            @Nonnull
            private Function<String, Optional<String>> resolvePackageType() {
                return new Function<String, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull String typeReference) {
                        StringBuilder buffy = new StringBuilder(typeReference);
                        prependPackageName(buffy);
                        return classPoolAccessor.resolveClass(buffy);
                    }
                };
            }

            @Nonnull
            private Function<String, Optional<String>> resolveAsteriskImports() {
                return new Function<String, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull String typeReference) {
                        for (ImportDeclaration importDeclaration :
                                filter(emptyIfNull(compilationUnit.getImports()), and(isAsterisk(), not(isStatic())))) {
                            StringBuilder buffy = new StringBuilder(typeReference);
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
            private Function<String, Optional<String>> resolveJavaLangType() {
                return new Function<String, Optional<String>>() {
                    @Nonnull
                    @Override
                    public Optional<String> apply(@SuppressWarnings("NullableProblems") @Nonnull String typeReference) {
                        return classPoolAccessor.resolveClass("java.lang." + typeReference);
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

            private StringBuilder prepend(NameExpr nameExpr, StringBuilder buffy) {
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


        }, null);
    }

    private static class TypeRecordingVisitor extends VoidVisitorAdapter<Void> {

        private final LinkedList<String> typeHierarchy = newLinkedList();
        private final Set<String> typeNames = newHashSet();
        private String outerMostType;

        @Override
        public void visit(AnnotationDeclaration n, Void arg) {
            visitTypeDefinition(n);
            try {
                super.visit(n, arg);
            } finally {
                unVisitTypeDefinition();
            }
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            visitTypeDefinition(n);
            try {
                super.visit(n, arg);
            } finally {
                unVisitTypeDefinition();
            }
        }

        @Override
        public void visit(EnumDeclaration n, Void arg) {
            visitTypeDefinition(n);
            try {
                super.visit(n, arg);
            } finally {
                unVisitTypeDefinition();
            }
        }

        private void visitTypeDefinition(TypeDeclaration typeDeclaration) {
            String typeName = typeDeclaration.getName();
            if (this.outerMostType == null) {
                this.outerMostType = typeName;
            }
            this.typeHierarchy.add(typeName);
            StringBuilder buffy = new StringBuilder();
            for (String type : typeHierarchy) {
                if (buffy.length() > 0) {
                    buffy.append('$');
                }
                buffy.append(type);
            }
            this.typeNames.add(buffy.toString());
        }

        private void unVisitTypeDefinition() {
            this.typeHierarchy.removeLast();
        }

        public String getOuterMostType() {
            return this.outerMostType;
        }

        public Set<String> getTypeNames() {
            return typeNames;
        }
    }

}
