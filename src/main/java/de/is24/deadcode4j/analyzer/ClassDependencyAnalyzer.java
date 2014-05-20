package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import javassist.CtClass;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Analyzes class files: lists the classes a class is depending on.
 *
 * @since 1.0.0
 */
public class ClassDependencyAnalyzer extends ByteCodeAnalyzer {

    @Override
    protected void analyzeClass(@Nonnull AnalysisContext analysisContext, @Nonnull CtClass clazz) {
        String className = clazz.getName();

        @SuppressWarnings("unchecked")
        Collection<String> refClasses = clazz.getRefClasses();

        analysisContext.addAnalyzedClass(className);
        analysisContext.addDependencies(className, refClasses);
    }

}
