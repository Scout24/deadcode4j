package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public class A_JerseyWebXmlAnalyzer extends AnAnalyzer<JerseyWebXmlAnalyzer> {

    @Override
    protected JerseyWebXmlAnalyzer createAnalyzer() {
        return new JerseyWebXmlAnalyzer();
    }

    @Test
    public void shouldParseWebXmlFiles() {
        analyzeFile("de/is24/deadcode4j/analyzer/jersey.web.xml");
        assertThatDependenciesAreReported(
                "jersey.dummy.filter.Application",
                "jersey.dummy.servlet.Application");
    }
}
