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
                "de.is24.deadcode4j.logback.Appender",
                "de.is24.deadcode4j.logback.ContextListener",
                "de.is24.deadcode4j.logback.CustomAction",
                "de.is24.deadcode4j.logback.CustomClass",
                "de.is24.deadcode4j.logback.Property",
                "de.is24.deadcode4j.logback.StatusListener"
        );
    }

}