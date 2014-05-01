package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import de.is24.deadcode4j.CodeContext;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
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
import java.io.File;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

/**
 * @since 1.6
 */
public class TypeErasureAnalyzer extends AnalyzerAdapter {

    @Override
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull File file) {

        if (file.getName().endsWith(".java")) {
            logger.debug("Analyzing Java file [{}]...", file);
            analyzeJavaFile(codeContext, file);
        }
    }

    private void analyzeJavaFile(@Nonnull final CodeContext codeContext, @Nonnull File file) {

        final CompilationUnit compilationUnit;
        try {
            compilationUnit = JavaParser.parse(file, null, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse [" + file + "]!", e);
        }

        compilationUnit.accept(new VoidVisitorAdapter<Object>() {
            private final ByteCodeAnalyzer byteCodeAnalyzer = new ByteCodeAnalyzer() {
                @Override
                protected void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
                    throw new UnsupportedOperationException();
                }
            };

            @Override
            public void visit(ClassOrInterfaceType n, Object arg) {
                List<Type> typeArguments = n.getTypeArgs();
                if (typeArguments == null)
                    return;
                for (Type type : typeArguments) {
                    if (!ReferenceType.class.isInstance(type)) {
                        continue;
                    }
                    Type nestedType = ReferenceType.class.cast(type).getType();
                    if (!ClassOrInterfaceType.class.isInstance(nestedType)) {
                        continue;
                    }
                    ClassOrInterfaceType nestedClassOrInterface = ClassOrInterfaceType.class.cast(nestedType);
                    codeContext.addDependencies(getTypeName(n), resolveClass(nestedClassOrInterface));
                    this.visit(nestedClassOrInterface, arg); // resolve nested type arguments
                }
            }

            private String getTypeName(ClassOrInterfaceType classOrInterfaceType) {
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
                if (compilationUnit.getPackage() != null) {
                    prependPackageName(compilationUnit, buffy);
                }
                return buffy.toString();
            }

            private String resolveClass(
                    @Nonnull ClassOrInterfaceType classOrInterfaceType) {
                ClassPool classPool = this.byteCodeAnalyzer.getOrCreateClassPool(codeContext);
                Optional<String> resolvedClass = resolveFullyQualifiedClass(classPool, classOrInterfaceType);
                if (resolvedClass.isPresent()) {
                    return resolvedClass.get();
                }
                resolvedClass = resolveInnerType(classPool, classOrInterfaceType);
                if (resolvedClass.isPresent()) {
                    return resolvedClass.get();
                }
                // fq
                // inner type
                // import
                // package
                // asterisk
                // java lang
                // default package
                return classOrInterfaceType.getName();
            }

            @Nonnull
            private Optional<String> resolveFullyQualifiedClass(@Nonnull ClassPool classPool,
                                                                @Nonnull ClassOrInterfaceType classOrInterfaceType) {
                if (classOrInterfaceType.getScope() == null)
                    return absent();
                return resolveClass(classPool, getQualifier(classOrInterfaceType));
            }

            @Nonnull
            private Optional<String> resolveInnerType(@Nonnull ClassPool classPool,
                                                      @Nonnull ClassOrInterfaceType classOrInterfaceType) {
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
                prependPackageName(compilationUnit, buffy);
                return resolveClass(classPool, buffy.toString());
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
            private StringBuilder prependPackageName(@Nonnull CompilationUnit compilationUnit,
                                                     @Nonnull StringBuilder buffy) {
                if (compilationUnit.getPackage() == null) {
                    return buffy;
                }
                NameExpr nameExpr = compilationUnit.getPackage().getName();
                do {
                    if (buffy.length() > 0) {
                        buffy.insert(0, '.');
                    }
                    buffy.insert(0, nameExpr.getName());

                }

                while (null != (nameExpr = QualifiedNameExpr.class.isInstance(nameExpr)
                        ? QualifiedNameExpr.class.cast(nameExpr).getQualifier() : null));
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
            private Optional<String> resolveClass(@Nonnull ClassPool classPool,
                                                  @Nonnull String qualifier) {
                for (; ; ) {
                    if (classPool.find(qualifier) != null) {
                        return of(qualifier);
                    }
                    int dotIndex = qualifier.lastIndexOf('.');
                    if (dotIndex < 0) {
                        return absent();
                    }
                    qualifier = qualifier.substring(0, dotIndex) + "$" + qualifier.substring(dotIndex + 1);
                }
            }
        }, null);
    }

}
