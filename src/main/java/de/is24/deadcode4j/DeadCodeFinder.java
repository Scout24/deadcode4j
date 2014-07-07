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
@SuppressWarnings("PMD.TooManyStaticImports")
public class DeadCodeFinder {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Iterable<? extends Analyzer> analyzers;

    public DeadCodeFinder(@Nonnull Set<? extends Analyzer> analyzers) {
        this.analyzers = newArrayList(analyzers);
    }

    @Nonnull
    public DeadCode findDeadCode(@Nonnull Iterable<Module> modules) {
        AnalyzedCode analyzedCode = analyzeCode(modules);
        return computeDeadCode(analyzedCode);
    }

    @Nonnull
    private AnalyzedCode analyzeCode(@Nonnull Iterable<Module> modules) {
        List<AnalyzedCode> analyzedCode = newArrayList();
        IntermediateResults intermediateResults = new IntermediateResults();
        for (Module module : sort(modules)) {
            AnalysisContext analysisContext = new AnalysisContext(module, intermediateResults.calculateIntermediateResultsFor(module));
            for (Repository repository : module.getAllRepositories()) {
                analyzeRepository(analysisContext, repository);
            }
            logger.debug("Finishing analysis of [{}]...", analysisContext);
            for (Analyzer analyzer : this.analyzers) {
                analyzer.finishAnalysis(analysisContext);
            }
            logger.debug("Finished analysis of [{}].", analysisContext);
            intermediateResults.add(analysisContext);
            analyzedCode.add(analysisContext.getAnalyzedCode());
        }
        logger.debug("Finishing analysis of whole project...");
        AnalyzedCode combinedAnalysis = merge(analyzedCode);
        for (Analyzer analyzer : this.analyzers) {
            analyzer.finishAnalysis(combinedAnalysis);
        }
        logger.debug("Finished analysis of project.");
        return combinedAnalysis;
    }

    @Nonnull
    private DeadCode computeDeadCode(@Nonnull AnalyzedCode analyzedCode) {
        Collection<String> deadClasses = determineDeadClasses(analyzedCode);
        return new DeadCode(analyzedCode, deadClasses);
    }

    private void analyzeRepository(@Nonnull AnalysisContext analysisContext, @Nonnull Repository repository) {
        RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer(analysisContext, repository, this.analyzers);
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
        private final AnalysisContext analysisContext;
        private final Repository repository;
        private final Iterable<? extends Analyzer> analyzers;

        public RepositoryAnalyzer(@Nonnull AnalysisContext analysisContext, @Nonnull Repository repository, @Nonnull Iterable<? extends Analyzer> analyzers) {
            super(repository.getFileFilter(), -1);
            this.repository = repository;
            this.analysisContext = analysisContext;
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
                    analyzer.doAnalysis(this.analysisContext, file);
                } catch (RuntimeException rE) {
                    logger.warn("Analyzer [{}] failed to analyze file [{}]!", analyzer, file, rE);
                    analysisContext.addException(AnalysisStage.FILE_ANALYSIS);
                }
            }
        }

        @Override
        protected void handleEnd(Collection<Void> results) {
            logger.debug("Analysis of [{}] is done.", this.repository);
        }

    }

}
