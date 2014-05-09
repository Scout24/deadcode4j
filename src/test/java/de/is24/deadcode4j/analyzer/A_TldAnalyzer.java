package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_TldAnalyzer extends AnAnalyzer<TldAnalyzer> {

    @Override
    protected TldAnalyzer createAnalyzer() {
        return new TldAnalyzer();
    }

    @Test
    public void shouldParseTldFiles() {
        objectUnderTest.doAnalysis(codeContext, getFile("taglib.tld"));

        assertThatDependenciesAreReported(
                "TagClass",
                "TagExtraInfo",
                "TagLibraryValidator",
                "TldFunction",
                "WebAppListener");
    }

}
