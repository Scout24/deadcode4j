package de.is24.deadcode4j;

import com.google.common.collect.Sets;
import javassist.ClassPool;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
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

    private final Set<Analyzer> analyzers;

    public DeadCodeFinder(@Nonnull Set<Analyzer> analyzers) {
        this.analyzers = analyzers;
    }

    public DeadCodeFinder() {
        this(Sets.newHashSet(new ClassFileAnalyzer(), new SpringXmlAnalyzer()));
    }

    public DeadCode findDeadCode(File... codeRepositories) {
        CodeContext codeContext = createCodeContext(codeRepositories);
        AnalyzedCode analyzedCode = analyzeCode(codeContext);
        return computeDeadCode(analyzedCode);
    }

    private CodeContext createCodeContext(File[] codeRepositories) {
        ClassPool classPool = setupJavassist(codeRepositories);
        ClassLoader classLoader = new URLClassLoader(toUrls(codeRepositories));
        return new CodeContext(codeRepositories, classLoader, classPool);
    }

    private AnalyzedCode analyzeCode(CodeContext codeContext) {
        AnalyzedCode analyzedCode = new AnalyzedCode(Collections.<String>emptyList(), Collections.<String, Iterable<String>>emptyMap());
        for (Analyzer analyzer : analyzers) {
            analyzedCode = analyzedCode.merge(analyzer.analyze(codeContext));
        }
        return analyzedCode;
    }

    private DeadCode computeDeadCode(AnalyzedCode analyzedCode) {
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
