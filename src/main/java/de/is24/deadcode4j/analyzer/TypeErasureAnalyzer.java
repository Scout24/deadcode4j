package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import de.is24.deadcode4j.CodeContext;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.Node;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import javassist.ClassPool;
import javassist.CtClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;

/**
 * Analyzes Java files and reports dependencies to classes that are not part of the byte code due to type erasure.
 * <b>Note</b> that references to "inherited" types are not analyzed, as they are found by the byte code analysis.
 *
 * @since 1.6
 */
public class TypeErasureAnalyzer extends AnalyzerAdapter {

    @Override
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull File file) {
        if (file.getName().endsWith(".java")) {
            logger.debug("Analyzing Java file [{}]...", file);
            final CompilationUnit compilationUnit;
            try {
                compilationUnit = JavaParser.parse(file, null, false);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse [" + file + "]!", e);
            }
            analyzeCompilationUnit(codeContext, compilationUnit);
        }
    }

    private void analyzeCompilationUnit(@Nonnull final CodeContext codeContext, @Nonnull final CompilationUnit compilationUnit) {
        compilationUnit.accept(new VoidVisitorAdapter<Object>() {
            private final ClassPool classPool = new ByteCodeAnalyzer() {
                @Override
                protected void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
                    throw new UnsupportedOperationException();
                }
            }.getOrCreateClassPool(codeContext);
            private final Deque<Set<String>> definedTypeParameters = newLinkedList();

            @Override
            public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
                try {
                    super.visit(n, arg);
                } finally {
                    this.definedTypeParameters.removeLast();
                }
            }

            @Override
            public void visit(ConstructorDeclaration n, Object arg) {
                this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
                try {
                    super.visit(n, arg);
                } finally {
                    this.definedTypeParameters.removeLast();
                }
            }

            @Override
            public void visit(MethodDeclaration n, Object arg) {
                this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
                try {
                    super.visit(n, arg);
                } finally {
                    this.definedTypeParameters.removeLast();
                }
            }

            @Override
            public void visit(ClassOrInterfaceType n, Object arg) {
                List<Type> typeArguments = n.getTypeArgs();
                if (typeArguments == null) {
                    return;
                }
                for (Type type : typeArguments) {
                    ClassOrInterfaceType referencedType = getReferencedType(type);
                    if (referencedType == null) {
                        continue;
                    }
                    if (typeParameterWithSameNameIsDefined(referencedType)) {
                        continue;
                    }
                    Optional<String> resolvedClass = resolveClass(referencedType);
                    if (resolvedClass.isPresent()) {
                        codeContext.addDependencies(getTypeName(n), resolvedClass.get());
                    } else {
                        logger.debug("Could not resolve Type Argument [{}] used by [{}].", referencedType, getTypeName(n));
                    }
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
                if (!ReferenceType.class.isInstance(type)) {
                    logger.debug("[{}:{}] is no ReferenceType.", type.getClass(), type);
                    return null;
                }
                Type nestedType = ReferenceType.class.cast(type).getType();
                if (!ClassOrInterfaceType.class.isInstance(nestedType)) {
                    logger.debug("[{}:{}] is no ClassOrInterfaceType.", type.getClass(), type);
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
            private Optional<String> resolveClass(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                Optional<String> resolvedClass = resolveFullyQualifiedClass(classOrInterfaceType);
                if (!resolvedClass.isPresent()) {
                    resolvedClass = resolveInnerType(classOrInterfaceType);
                }
                if (!resolvedClass.isPresent()) {
                    resolvedClass = resolveImport(classOrInterfaceType);
                }
                if (!resolvedClass.isPresent()) {
                    resolvedClass = resolvePackageType(classOrInterfaceType);
                }
                if (!resolvedClass.isPresent()) {
                    resolvedClass = resolveAsteriskImports(classOrInterfaceType);
                }
                if (!resolvedClass.isPresent()) {
                    resolvedClass = resolveJavaLangType(classOrInterfaceType);
                }
                return resolvedClass;
            }

            @Nonnull
            private Optional<String> resolveFullyQualifiedClass(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                if (classOrInterfaceType.getScope() == null)
                    return absent();
                return resolveClass(getQualifier(classOrInterfaceType));
            }

            @Nonnull
            private Optional<String> resolveInnerType(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                String outermostType = null;
                Node node = classOrInterfaceType;
                while ((node = node.getParentNode()) != null) {
                    if (TypeDeclaration.class.isInstance(node)) {
                        outermostType = TypeDeclaration.class.cast(node).getName();
                    }
                }
                assert outermostType != null;
                StringBuilder buffy = new StringBuilder(outermostType)
                        .append('$')
                        .append(getQualifier(classOrInterfaceType));
                prependPackageName(buffy);
                return resolveClass(buffy);
            }

            @Nonnull
            private Optional<String> resolveImport(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                if (compilationUnit.getImports() == null)
                    return absent();
                String referencedClass = getQualifier(classOrInterfaceType);
                for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
                    if (importDeclaration.isAsterisk()) {
                        continue;
                    }
                    String importedClass = importDeclaration.getName().getName();
                    if (importedClass.equals(referencedClass) || referencedClass.startsWith(importedClass + ".")) {
                        StringBuilder buffy = prepend(importDeclaration.getName(), new StringBuilder());
                        buffy.append(referencedClass.substring(importedClass.length()));
                        return resolveClass(buffy);
                    }
                }
                return absent();
            }

            @Nonnull
            private Optional<String> resolvePackageType(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                StringBuilder buffy = new StringBuilder(getQualifier(classOrInterfaceType));
                prependPackageName(buffy);
                return resolveClass(buffy);
            }

            @Nonnull
            private Optional<String> resolveAsteriskImports(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                if (compilationUnit.getImports() == null)
                    return absent();
                String referencedClass = getQualifier(classOrInterfaceType);
                for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
                    if (!importDeclaration.isAsterisk() || importDeclaration.isStatic()) {
                        continue;
                    }
                    StringBuilder buffy = new StringBuilder(referencedClass);
                    prepend(importDeclaration.getName(), buffy);
                    Optional<String> resolvedClass = resolveClass(buffy);
                    if (resolvedClass.isPresent()) {
                        return resolvedClass;
                    }
                }
                return absent();
            }

            @Nonnull
            private Optional<String> resolveJavaLangType(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                return resolveClass("java.lang." + getQualifier(classOrInterfaceType));
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

            /**
             * Returns the "resolved" class name for the given qualifier.
             * "Resolved" in this case means that if the qualifier refers to an existing class, the class'
             * {@link java.lang.ClassLoader binary name} is returned.
             *
             * @since 1.6
             */
            @Nonnull
            private Optional<String> resolveClass(@Nonnull String qualifier) {
                for (; ; ) {
                    if (this.classPool.getOrNull(qualifier) != null) {
                        return of(qualifier);
                    }
                    int dotIndex = qualifier.lastIndexOf('.');
                    if (dotIndex < 0) {
                        return absent();
                    }
                    qualifier = qualifier.substring(0, dotIndex) + "$" + qualifier.substring(dotIndex + 1);
                }
            }


            @Nonnull
            private Optional<String> resolveClass(@Nonnull CharSequence qualifier) {
                return resolveClass(qualifier.toString());
            }
        }, null);
    }

}
