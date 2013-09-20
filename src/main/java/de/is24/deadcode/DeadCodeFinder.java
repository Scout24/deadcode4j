package de.is24.deadcode;

import javassist.ClassPool;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class DeadCodeFinder {

    public DeadCodeFinder() {
    }

    public DeadCode findDeadCode(File... codeRepositories) {
        ClassPool classPool = setupJavassist(codeRepositories);
        DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(classPool, codeRepositories);

        AnalyzedCode analyzedCode = dependencyAnalyzer.analyze();
        Iterable<String> deadClasses = determineDeadClasses(analyzedCode);

        return new DeadCode(analyzedCode.getAnalyzedClasses(), deadClasses);
    }

    private ClassPool setupJavassist(@Nonnull File[] codeRepositories) {
        ClassPool classPool = new ClassPool(false);

        for (File codeRepository : codeRepositories) {
            try {
                classPool.appendClassPath(codeRepository.getAbsolutePath());
            } catch (NotFoundException e) {
                throw new IllegalArgumentException("The given code directory [" + codeRepository + "] cannot be used!", e);
            }
        }
        return classPool;
    }

    @Nonnull
    private Iterable<String> determineDeadClasses(@Nonnull AnalyzedCode analyzedCode) {
        Set<String> classesInUse = newHashSet();
        for (Iterable<String> usedClasses : analyzedCode.getDependenciesForClass().values()) {
            for (String clazz : usedClasses) {
                classesInUse.add(clazz);
            }
        }

        List<String> deadClasses = newArrayList(analyzedCode.getAnalyzedClasses());
        deadClasses.removeAll(classesInUse);
        return deadClasses;
    }

}
