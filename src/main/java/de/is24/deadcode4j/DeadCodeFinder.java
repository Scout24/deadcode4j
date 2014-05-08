package de.is24.deadcode4j;

import org.apache.commons.io.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Module.sort;
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
        for (Module module : sort(modules)) {
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
        return new DeadCode(analyzedCode.getStagesWithExceptions(), analyzedCode.getAnalyzedClasses(), deadClasses);
    }

    private void analyzeRepository(@Nonnull CodeContext codeContext, @Nonnull Repository repository) {
        RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer(codeContext, repository, this.analyzers);
        try {
            repositoryAnalyzer.analyze();
        } catch (IOException e) {
            throw new RuntimeException("This was unexpected; failed to parse files of " + repository + "!", e);
        }
    }

    private AnalyzedCode merge(List<AnalyzedCode> analyzedCode) {
        EnumSet<AnalysisStage> stagesWithExceptions = EnumSet.noneOf(AnalysisStage.class);
        Set<String> analyzedClasses = newHashSet();
        Map<String, Set<String>> dependencies = newHashMap();
        for (AnalyzedCode code : analyzedCode) {
            stagesWithExceptions.addAll(code.getStagesWithExceptions());
            analyzedClasses.addAll(code.getAnalyzedClasses());
            for (Map.Entry<String, Set<String>> dependencyEntry : code.getCodeDependencies().entrySet()) {
                Set<String> knownDependencies = getOrAddMappedSet(dependencies, dependencyEntry.getKey());
                knownDependencies.addAll(dependencyEntry.getValue());
            }
        }
        return new AnalyzedCode(stagesWithExceptions, analyzedClasses, dependencies);
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

    private static class RepositoryAnalyzer extends DirectoryWalker<Void> {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final CodeContext codeContext;
        private final Repository repository;
        private final Iterable<? extends Analyzer> analyzers;

        public RepositoryAnalyzer(@Nonnull CodeContext codeContext, @Nonnull Repository repository, @Nonnull Iterable<? extends Analyzer> analyzers) {
            super(repository.getFileFilter(), -1);
            this.repository = repository;
            this.codeContext = codeContext;
            this.analyzers = analyzers;
        }

        public void analyze() throws IOException {
            logger.debug("Starting analysis of [{}]...", this.repository);
            super.walk(this.repository.getDirectory(), null);
        }

        @Override
        protected void handleFile(File file, int depth, Collection results) {
            logger.debug("Analyzing file [{}]...", file);
            for (Analyzer analyzer : this.analyzers) {
                try {
                    analyzer.doAnalysis(this.codeContext, file);
                } catch (RuntimeException rE) {
                    logger.warn("Analyzer [{}] failed to analyze file [{}]!", analyzer, file, rE);
                    codeContext.addException(AnalysisStage.FILE_ANALYSIS);
                }
            }
        }

        @Override
        protected void handleEnd(Collection<Void> results) {
            logger.debug("Finishing analysis of [{}]...", this.repository);
            for (Analyzer analyzer : this.analyzers)
                analyzer.finishAnalysis(this.codeContext);
            logger.debug("Finished analysis of [{}].", this.repository);
        }

    }

}
