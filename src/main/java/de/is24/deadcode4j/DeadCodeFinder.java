package de.is24.deadcode4j;

import org.apache.commons.io.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.getOrAddMappedSet;

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
    public DeadCode findDeadCode(@Nonnull Iterable<Module> modules) {
        AnalyzedCode analyzedCode = analyzeCode(modules);
        return computeDeadCode(analyzedCode);
    }

    @Nonnull
    private AnalyzedCode analyzeCode(@Nonnull Iterable<Module> modules) {
        List<AnalyzedCode> analyzedCode = newArrayList();
        for (Module module : modules) {
            CodeContext codeContext = new CodeContext(module);
            for (Repository repository : module.getAllRepositories()) {
                analyzeRepository(codeContext, repository);
            }
            analyzedCode.add(codeContext.getAnalyzedCode());
        }
        return merge(analyzedCode);
    }

    @Nonnull
    private DeadCode computeDeadCode(@Nonnull AnalyzedCode analyzedCode) {
        Collection<String> deadClasses = determineDeadClasses(analyzedCode);
        return new DeadCode(analyzedCode.getAnalyzedClasses(), deadClasses);
    }

    private void analyzeRepository(@Nonnull CodeContext codeContext, @Nonnull Repository repository) {
        RepositoryAnalyzer repositoryAnalyzer =
                new RepositoryAnalyzer(repository.getFileFilter(), this.analyzers, codeContext);
        try {
            repositoryAnalyzer.analyze(repository.getDirectory());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse files of " + repository + "!", e);
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

    private AnalyzedCode merge(List<AnalyzedCode> analyzedCode) {
        Set<String> analyzedClasses = newHashSet();
        Map<String, Set<String>> dependencies = newHashMap();
        for (AnalyzedCode code : analyzedCode) {
            analyzedClasses.addAll(code.getAnalyzedClasses());
            for (Map.Entry<String, Set<String>> dependencyEntry : code.getCodeDependencies().entrySet()) {
                Set<String> knownDependencies = getOrAddMappedSet(dependencies, dependencyEntry.getKey());
                knownDependencies.addAll(dependencyEntry.getValue());
            }
        }
        return new AnalyzedCode(analyzedClasses, dependencies);
    }

    private static class RepositoryAnalyzer extends DirectoryWalker<Void> {
        private final Iterable<? extends Analyzer> analyzers;
        private final CodeContext codeContext;
        private Logger logger = LoggerFactory.getLogger(getClass());

        public RepositoryAnalyzer(@Nonnull FileFilter fileFilter, @Nonnull Iterable<? extends Analyzer> analyzers, @Nonnull CodeContext codeContext) {
            super(fileFilter, -1);
            this.analyzers = analyzers;
            this.codeContext = codeContext;
        }

        public void analyze(File codeRepository) throws IOException {
            super.walk(codeRepository, null);
        }

        @Override
        protected void handleFile(File file, int depth, Collection results) {
            logger.debug("Analyzing file [{}]...", file);
            for (Analyzer analyzer : this.analyzers) {
                try {
                    analyzer.doAnalysis(this.codeContext, file);
                } catch (RuntimeException rE) {
                    logger.warn("Analyzer [{}] failed to analyze file [{}]!", analyzer, file, rE);
                    throw rE;
                }
            }
        }

        @Override
        protected void handleEnd(Collection<Void> results) {
            for (Analyzer analyzer : this.analyzers)
                analyzer.finishAnalysis(this.codeContext);
        }

    }

}
