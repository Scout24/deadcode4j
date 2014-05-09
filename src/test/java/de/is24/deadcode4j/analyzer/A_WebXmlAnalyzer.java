package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_WebXmlAnalyzer extends AnAnalyzer<WebXmlAnalyzer> {

    @Override
    protected WebXmlAnalyzer createAnalyzer() {
        return new WebXmlAnalyzer();
    }

    @Test
    public void shouldParseWebXmlFiles() {
        objectUnderTest.doAnalysis(codeContext, getFile("web.xml"));

        assertThatDependenciesAreReported(
                "WebAppListener",
                "WebAppFilter",
                "WebAppServlet");
    }

}
