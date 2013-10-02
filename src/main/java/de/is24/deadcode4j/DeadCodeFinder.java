package de.is24.deadcode4j;

import com.google.common.collect.Sets;
import javassist.ClassPool;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.toUrls;

/**
 * The <code>DeadCodeFinder</code> ties everything together in order to ultimately find dead code.
 *
 * @since 1.0.0
 */
public class DeadCodeFinder {

    private final Set<? extends Analyzer> analyzers;

    public DeadCodeFinder(@Nonnull Set<? extends Analyzer> analyzers) {
        this.analyzers = analyzers;
    }

    public DeadCodeFinder() {
        this(Sets.newHashSet(new ClassFileAnalyzer(), new SpringXmlAnalyzer()));
    }

    @Nonnull
    public DeadCode findDeadCode(File... codeRepositories) {
        CodeContext codeContext = createCodeContext(codeRepositories);
        AnalyzedCode analyzedCode = analyzeCode(codeContext);
        return computeDeadCode(analyzedCode);
    }

    @Nonnull
    private CodeContext createCodeContext(@Nonnull File[] codeRepositories) {
        ClassPool classPool = setupJavassist(codeRepositories);
        ClassLoader classLoader = new URLClassLoader(toUrls(codeRepositories));
        return new CodeContext(codeRepositories, classLoader, classPool);
    }

    @Nonnull
    private AnalyzedCode analyzeCode(@Nonnull CodeContext codeContext) {
        AnalyzedCode analyzedCode = new AnalyzedCode(Collections.<String>emptyList(), Collections.<String, Iterable<String>>emptyMap());
        for (Analyzer analyzer : analyzers) {
            analyzedCode = analyzedCode.merge(analyzer.analyze(codeContext));
        }
        return analyzedCode;
    }

    @Nonnull
    private DeadCode computeDeadCode(@Nonnull AnalyzedCode analyzedCode) {
        Collection<String> deadClasses = determineDeadClasses(analyzedCode);
        return new DeadCode(analyzedCode.getAnalyzedClasses(), deadClasses);
    }

    @Nonnull
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
