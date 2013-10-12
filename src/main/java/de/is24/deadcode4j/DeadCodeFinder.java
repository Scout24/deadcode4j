package de.is24.deadcode4j;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.IOFileFilter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

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
    public DeadCode findDeadCode(CodeRepository... codeRepositories) {
        AnalyzedCode analyzedCode = analyzeCode(new CodeContext(), codeRepositories);
        return computeDeadCode(analyzedCode);
    }

    @Nonnull
    private AnalyzedCode analyzeCode(@Nonnull CodeContext codeContext, @Nonnull CodeRepository[] codeRepositories) {
        for (CodeRepository codeRepository : codeRepositories) {
            analyzeRepository(codeContext, codeRepository);
        }

        return codeContext.getAnalyzedCode();
    }

    @Nonnull
    private DeadCode computeDeadCode(@Nonnull AnalyzedCode analyzedCode) {
        Collection<String> deadClasses = determineDeadClasses(analyzedCode);
        return new DeadCode(analyzedCode.getAnalyzedClasses(), deadClasses);
    }

    private void analyzeRepository(@Nonnull CodeContext codeContext, @Nonnull CodeRepository codeRepository) {
        CodeRepositoryAnalyzer codeRepositoryAnalyzer =
                new CodeRepositoryAnalyzer(codeRepository.getFileFilter(), this.analyzers, codeContext);
        try {
            codeRepositoryAnalyzer.analyze(codeRepository.getDirectory());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse files of " + codeRepository + "!", e);
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

    private static class CodeRepositoryAnalyzer extends DirectoryWalker {

        private final Iterable<? extends Analyzer> analyzers;
        private final CodeContext codeContext;

        public CodeRepositoryAnalyzer(@Nonnull IOFileFilter fileFilter, @Nonnull Iterable<? extends Analyzer> analyzers, @Nonnull CodeContext codeContext) {
            super(fileFilter, -1);
            this.analyzers = analyzers;
            this.codeContext = codeContext;
        }

        public void analyze(File codeRepository) throws IOException {
            super.walk(codeRepository, null);
        }

        @Override
        protected void handleFile(File file, int depth, Collection results) throws IOException {
            for (Analyzer analyzer : this.analyzers)
                analyzer.doAnalysis(this.codeContext, file);
        }

    }

}
