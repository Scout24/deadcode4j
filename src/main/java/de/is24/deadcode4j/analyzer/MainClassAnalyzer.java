package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import javassist.CtClass;
import javassist.Modifier;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.MethodInfo;

import javax.annotation.Nonnull;

import static com.google.common.collect.Iterables.filter;

/**
 * Analyzes class files: marks a class as being in use if it defines a main method.
 *
 * @since 2.0.0
 */
public class MainClassAnalyzer extends ByteCodeAnalyzer {

    private static boolean isPublicStatic(MethodInfo methodInfo) {
        int modifier = AccessFlag.toModifier(methodInfo.getAccessFlags());
        return Modifier.isPublic(modifier) && Modifier.isStatic(modifier);
    }

    private static boolean matchesSignature(MethodInfo methodInfo) {
        return "([Ljava/lang/String;)V".equals(methodInfo.getDescriptor());
    }

    @Override
    protected void analyzeClass(@Nonnull AnalysisContext analysisContext, @Nonnull CtClass clazz) {
        String clazzName = clazz.getName();
        analysisContext.addAnalyzedClass(clazzName);

        for (MethodInfo methodInfo : filter(clazz.getClassFile2().getMethods(), MethodInfo.class)) {
            if (methodInfo.isMethod()
                    && isPublicStatic(methodInfo)
                    && "main".equals(methodInfo.getName())
                    && matchesSignature(methodInfo)) {
                analysisContext.addDependencies("_Main-Class_", clazz.getName());
            }
        }
    }

}
