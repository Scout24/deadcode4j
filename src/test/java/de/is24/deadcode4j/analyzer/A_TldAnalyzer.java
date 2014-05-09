package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_TldAnalyzer extends AnAnalyzer {

    @Test
    public void shouldParseTldFiles() {
        TldAnalyzer objectUnderTest = new TldAnalyzer();

        objectUnderTest.doAnalysis(codeContext, getFile("taglib.tld"));

        assertThatDependenciesAreReported(
                "TagClass",
                "TagExtraInfo",
                "TagLibraryValidator",
                "TldFunction",
                "WebAppListener");
    }

}
