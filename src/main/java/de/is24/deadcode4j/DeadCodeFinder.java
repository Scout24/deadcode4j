package de.is24.deadcode4j;

import javassist.ClassPool;
import javassist.NotFoundException;
import org.codehaus.plexus.util.DirectoryScanner;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URLClassLoader;
import java.util.Collection;
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

    @Nonnull
    public DeadCode findDeadCode(File... codeRepositories) {
        CodeContext codeContext = createCodeContext(codeRepositories);
        AnalyzedCode analyzedCode = analyzeCode(codeContext, codeRepositories);
        return computeDeadCode(analyzedCode);
    }

    @Nonnull
    private CodeContext createCodeContext(@Nonnull File[] codeRepositories) {
        ClassPool classPool = setupJavassist(codeRepositories);
        ClassLoader classLoader = new URLClassLoader(toUrls(codeRepositories));
        return new CodeContext(classLoader, classPool);
    }

    @Nonnull
    private AnalyzedCode analyzeCode(@Nonnull CodeContext codeContext, @Nonnull File[] codeRepositories) {
        for (File codeRepository : codeRepositories) {
            analyzeRepository(codeContext, codeRepository);
        }

        return codeContext.getAnalyzedCode();
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

    private void analyzeRepository(@Nonnull CodeContext codeContext, @Nonnull File codeRepository) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(codeRepository);
        if (codeRepository.getAbsolutePath().endsWith("WEB-INF")) {
            scanner.setExcludes(new String[]{"classes/**"});
        }
        scanner.scan();

        for (String file : scanner.getIncludedFiles()) {
            for (Analyzer analyzer : analyzers) {
                analyzer.doAnalysis(codeContext, file);
            }
        }
    }

    @Nonnull
    Collection<String> determineDeadClasses(@Nonnull AnalyzedCode analyzedCode) {
        Set<String> classesInUse = newHashSet();
        for (Iterable<String> usedClasses : analyzedCode.getCodeDependencies().values()) {
            for (String clazz : usedClasses) {
                classesInUse.add(clazz);
            }
        }

        List<String> deadClasses = newArrayList(analyzedCode.getAnalyzedClasses());
        deadClasses.removeAll(classesInUse);
        return deadClasses;
    }

}
