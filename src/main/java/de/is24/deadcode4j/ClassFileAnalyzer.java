package de.is24.deadcode4j;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.util.Collection;

import static java.util.Collections.emptyList;

/**
 * Analyzes class files: lists the classes a class is depending on.
 *
 * @since 1.0.0
 */
public class ClassFileAnalyzer implements Analyzer {

    @Override
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull String fileName) {
        if (fileName.endsWith(".class")) {
            analyzeClass(codeContext, fileName.substring(0, fileName.length() - 6).replace('/', '.'));
        }
    }

    @SuppressWarnings("unchecked")
    private void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull String clazz) {
        codeContext.addAnalyzedClass(clazz);
        CtClass ctClass = getClassFor(codeContext.getClassPool(), clazz);
        Collection refClasses = ctClass.getRefClasses();
        if (refClasses == null) {
            refClasses = emptyList();
        } else {
            refClasses.remove(clazz);
        }

        codeContext.addDependencies(clazz, refClasses);
    }

    @Nonnull
    private CtClass getClassFor(@Nonnull ClassPool classPool, @Nonnull String clazz) {
        try {
            return classPool.get(clazz);
        } catch (NotFoundException e) {
            throw new RuntimeException("Could not load class [" + clazz + "]!", e);
        }
    }

}
