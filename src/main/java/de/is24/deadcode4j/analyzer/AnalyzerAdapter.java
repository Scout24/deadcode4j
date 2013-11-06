package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;

import javax.annotation.Nonnull;

/**
 * The <code>AnalyzerAdapter</code> implements all non-vital methods defined for an <code>Analyzer</code> with a no-op.
 *
 * @since 1.4
 */
public abstract class AnalyzerAdapter implements Analyzer {

    @Override
    public void finishAnalysis(@Nonnull CodeContext codeContext) {
    }

}
