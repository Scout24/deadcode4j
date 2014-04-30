package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

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

        CompilationUnit compilationUnit;
        try {
            compilationUnit = JavaParser.parse(file, null, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse [" + file + "]!", e);
        }

        compilationUnit.accept(new VoidVisitorAdapter<Object>() {
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
                            resolveClass(nestedClassOrInterface.getName()));
                    this.visit(nestedClassOrInterface, arg); // resolve nested type arguments
                }
            }

            private String resolveClass(String name) {
                return name;
            }
        }, null);
    }

}
