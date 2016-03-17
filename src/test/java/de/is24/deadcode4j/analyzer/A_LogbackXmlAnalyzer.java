package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public class A_LogbackXmlAnalyzer extends AnAnalyzer<LogbackXmlAnalyzer> {

    @Override
    protected LogbackXmlAnalyzer createAnalyzer() {
        return new LogbackXmlAnalyzer();
    }

    @Test
    public void shouldParseLogbackFiles() {
        analyzeFile("de/is24/deadcode4j/analyzer/logback.xml");

        assertThatDependenciesAreReported(
                "dummy.logback.Property",
                "dummy.logback.ContextListener",
                "dummy.logback.StatusListener",
                "dummy.logback.Appender");
    }
}