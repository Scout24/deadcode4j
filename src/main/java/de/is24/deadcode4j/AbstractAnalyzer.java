package de.is24.deadcode4j;

import javax.annotation.Nonnull;
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
     * @since 1.0.2
     */
    protected AbstractAnalyzer() {
        super();
    }

    @Nonnull
    public final AnalyzedCode getAnalyzedCode() {
        return new AnalyzedCode(getAnalyzedClasses(), getClassDependencies());
    }

    /**
     * The analyzed classes.
     * This method will be called <b>after</b> calling {@link #doAnalysis(CodeContext, String)} for each file to analyze.
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
     * This method will be called <b>after</b> calling {@link #doAnalysis(CodeContext, String)} for each file to analyze.
     *
     * @since 1.0.2
     */
    @Nonnull
    protected abstract Map<String, ? extends Iterable<String>> getClassDependencies();

}
