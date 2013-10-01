package de.is24.deadcode4j;

import javassist.ClassPool;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class DeadCodeFinder {

    private static URL[] toUrls(@Nonnull File[] codeRepositories) {
        URL[] urls = new URL[codeRepositories.length];
        for (int i = urls.length; i-- > 0; )
            try {
                urls[i] = codeRepositories[i].toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to set up code repositories!", e);
            }
        return urls;
    }

    public DeadCodeFinder() {
    }

    public DeadCode findDeadCode(File... codeRepositories) {
        ClassPool classPool = setupJavassist(codeRepositories);
        ClassLoader classLoader = new URLClassLoader(toUrls(codeRepositories));
        ClassFileAnalyzer classFileAnalyzer = new ClassFileAnalyzer(classPool, codeRepositories);
        SpringXmlAnalyzer springXmlAnalyzer = new SpringXmlAnalyzer(classLoader, codeRepositories);

        AnalyzedCode analyzedCode = classFileAnalyzer.analyze();
        analyzedCode = analyzedCode.merge(springXmlAnalyzer.analyze());
        Collection<String> deadClasses = determineDeadClasses(analyzedCode);

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
    private Collection<String> determineDeadClasses(@Nonnull AnalyzedCode analyzedCode) {
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
