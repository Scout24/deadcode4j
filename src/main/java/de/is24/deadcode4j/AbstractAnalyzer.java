package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * The <code>AbstractAnalyzer</code> implements a common approach useful for most analyzers.
 *
 * @since 1.0.2
 */
public abstract class AbstractAnalyzer implements Analyzer {

    /**
     * The <code>CodeContext</code> to use for analysis.
     *
     * @since 1.0.2
     */
    protected CodeContext codeContext;

    /**
     * @since 1.0.2
     */
    protected AbstractAnalyzer() {
        super();
    }

    @Nonnull
    public final AnalyzedCode analyze(@Nonnull CodeContext codeContext) {
        this.codeContext = codeContext;

        for (File codeRepository : codeContext.getCodeRepositories()) {
            analyzeRepository(codeRepository);
        }

        return new AnalyzedCode(getAnalyzedClasses(), getClassDependencies());
    }

    /**
     * Perform an analysis for the specified file.
     *
     * @since 1.0.2
     */
    protected abstract void doAnalysis(@Nonnull String fileName);

    /**
     * The analyzed classes.
     * This method will be called <b>after</b> calling {@link #doAnalysis(String)} for each file to analyze.
     * <p/>
     * Returns an empty list if not overridden.
     *
     * @since 1.0.2
     */
    @Nonnull
    protected Collection<String> getAnalyzedClasses() {
        return emptyList();
    }

    /**
     * The computed class dependencies.
     * This method will be called <b>after</b> calling {@link #doAnalysis(String)} for each file to analyze.
     *
     * @since 1.0.2
     */
    @Nonnull
    protected abstract Map<String, ? extends Iterable<String>> getClassDependencies();

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
        doAnalysis(fileName);
    }


}
