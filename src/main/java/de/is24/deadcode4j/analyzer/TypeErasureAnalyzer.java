package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import de.is24.deadcode4j.CodeContext;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
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

    private void analyzeJavaFile(final CodeContext codeContext, File file) {

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
                    codeContext.addDependencies("de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList",
                            resolveClass(nestedClassOrInterface));
                    this.visit(nestedClassOrInterface, arg); // resolve nested type arguments
                }
            }

            private String resolveClass(ClassOrInterfaceType classOrInterfaceType) {
                ClassPool classPool = this.byteCodeAnalyzer.getOrCreateClassPool(codeContext);
                Optional<String> resolvedClass = resolveFullyQualifiedClass(classPool, classOrInterfaceType);
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
            private Optional<String> resolveFullyQualifiedClass(
                    @Nonnull ClassPool classPool,
                    @Nonnull ClassOrInterfaceType classOrInterfaceType) {
                if (classOrInterfaceType.getScope() == null)
                    return absent();
                StringBuilder buffy = new StringBuilder(classOrInterfaceType.getName());
                while ((classOrInterfaceType = classOrInterfaceType.getScope()) != null) {
                    buffy.insert(0, '.');
                    buffy.insert(0, classOrInterfaceType.getName());
                }
                return resolveClass(classPool, buffy.toString());
            }

            /**
             * Returns the "resolved" class name for the given qualifier.
             * "Resolved" in this case means that if the qualifier refers to an existing class, the class'
             * {@link java.lang.ClassLoader binary name} is returned.
             *
             * @since 1.6
             */
            private Optional<String> resolveClass(ClassPool classPool, String qualifier) {
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
