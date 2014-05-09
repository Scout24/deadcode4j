package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_WebXmlAnalyzer extends AnAnalyzer {

    @Test
    public void shouldParseWebXmlFiles() {
        WebXmlAnalyzer objectUnderTest = new WebXmlAnalyzer();

        objectUnderTest.doAnalysis(codeContext, getFile("web.xml"));

        assertThatDependenciesAreReported(
                "WebAppListener",
                "WebAppFilter",
                "WebAppServlet");
    }

}
