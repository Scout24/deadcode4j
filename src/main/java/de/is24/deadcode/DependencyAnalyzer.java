package de.is24.deadcode;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;

public class DependencyAnalyzer {
    private final ClassPool classPool;
    private final File[] codeRepositories;
    private final List<String> analyzedClasses = newArrayList();
    private final Map<String, Iterable<String>> dependenciesForClass = newHashMap();

    public DependencyAnalyzer(@Nonnull ClassPool classPool, @Nonnull File[] codeRepositories) {
        this.classPool = classPool;
        this.codeRepositories = codeRepositories;
    }

    @Nonnull
    public AnalyzedCode analyze() {
        for (File codeRepository : codeRepositories) {
            analyzeRepository(codeRepository);
        }

        return new AnalyzedCode(analyzedClasses, dependenciesForClass);
    }

    private void analyzeRepository(@Nonnull File codeRepository) {
        analyzeFile(codeRepository, codeRepository);

    }

    private void analyzeFile(@Nonnull File codeRepository, @Nonnull File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File childNode : children) {
                    analyzeFile(codeRepository, childNode);
                }
            }
            return;
        }
        String fileName = file.getAbsolutePath().substring(codeRepository.getAbsolutePath().length() + 1);
        if (fileName.endsWith(".class")) {
            analyzeClass(fileName.substring(0, fileName.length() - 6).replace('/', '.'));
        }
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
        final CtClass ctClass;
        try {
            ctClass = this.classPool.get(clazz);
        } catch (NotFoundException e) {
            throw new RuntimeException("Could not load class [" + clazz + "]!", e);
        }
        return ctClass;
    }

}
