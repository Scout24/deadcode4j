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

            private String resolveClass(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
                Optional<String> resolvedClass = resolveFullyQualifiedClass(classOrInterfaceType);
                if (resolvedClass.isPresent()) {
                    return resolvedClass.get();
                }
                resolvedClass = resolveInnerType(classOrInterfaceType);
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
                return resolveClass(buffy.toString());
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
                NameExpr nameExpr = compilationUnit.getPackage().getName();
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
                    if (this.classPool.find(qualifier) != null) {
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