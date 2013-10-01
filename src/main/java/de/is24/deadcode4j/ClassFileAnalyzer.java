package de.is24.deadcode4j;

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

    @Override
    protected void doAnalysis(@Nonnull String fileName) {
        if (fileName.endsWith(".class")) {
            analyzeClass(fileName.substring(0, fileName.length() - 6).replace('/', '.'));
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
    private void analyzeClass(@Nonnull String clazz) {
        analyzedClasses.add(clazz);
        CtClass ctClass = getClassFor(clazz);
        Collection refClasses = ctClass.getRefClasses();
        if (refClasses == null) {
            refClasses = emptyList();
        } else {
            refClasses.remove(clazz);
        }

        dependenciesForClass.put(clazz, refClasses);
    }

    @Nonnull
    private CtClass getClassFor(@Nonnull String clazz) {
        try {
            return super.codeContext.getClassPool().get(clazz);
        } catch (NotFoundException e) {
            throw new RuntimeException("Could not load class [" + clazz + "]!", e);
        }
    }

}
