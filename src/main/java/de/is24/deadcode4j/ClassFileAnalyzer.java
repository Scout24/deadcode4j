package de.is24.deadcode4j;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;

/**
 * Analyzes class files: lists the classes a class is depending on.
 *
 * @since 1.0.0
 */
public class ClassFileAnalyzer extends AbstractAnalyzer {
    private final List<String> analyzedClasses = newArrayList();
    private final Map<String, Iterable<String>> dependenciesForClass = newHashMap();

    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull String fileName) {
        if (fileName.endsWith(".class")) {
            analyzeClass(codeContext.getClassPool(), fileName.substring(0, fileName.length() - 6).replace('/', '.'));
        }
    }

    @Nonnull
    @Override
    protected Collection<String> getAnalyzedClasses() {
        return this.analyzedClasses;
    }

    @Nonnull
    @Override
    protected Map<String, ? extends Iterable<String>> getClassDependencies() {
        return dependenciesForClass;
    }

    @SuppressWarnings("unchecked")
    private void analyzeClass(@Nonnull ClassPool classPool, @Nonnull String clazz) {
        analyzedClasses.add(clazz);
        CtClass ctClass = getClassFor(classPool, clazz);
        Collection refClasses = ctClass.getRefClasses();
        if (refClasses == null) {
            refClasses = emptyList();
        } else {
            refClasses.remove(clazz);
        }

        dependenciesForClass.put(clazz, refClasses);
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
