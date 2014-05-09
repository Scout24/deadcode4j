package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import org.junit.Before;

public abstract class AFinalAnalyzer<T extends Analyzer> extends AnAnalyzer {

    protected T objectUnderTest;

    @Before
    public final void initAnalyzer() {
        objectUnderTest = createAnalyzer();
    }

    protected abstract T createAnalyzer();

    protected void finishAnalysis() {
        this.objectUnderTest.finishAnalysis(super.codeContext);
    }

}
