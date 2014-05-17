package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Arrays;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public final class A_CustomInterfacesAnalyzer extends AnAnalyzer<CustomInterfacesAnalyzer> {

    private Log log;

    @Override
    public LoggingRule enableLogging() {
        log = LoggingRule.createMock();
        return new LoggingRule(log);
    }

    @Override
    protected CustomInterfacesAnalyzer createAnalyzer() {
        return new CustomInterfacesAnalyzer(Arrays.asList("foo.Bar"));
    }

    @After
    public void resetMock() {
        reset(log);
    }

    @Test
    public void logsObsoleteInterfaceEntry() {
        analyzeFile("SomeServletInitializer.class");
        doFinishAnalysis();

        verify(log).warn(Matchers.contains("foo.Bar"));
    }

    @Test
    public void doesNotLogInterfaceEntryFoundInClassPath() {
        this.objectUnderTest = new CustomInterfacesAnalyzer(Arrays.asList("de.is24.deadcode4j.junit.SomeInterface"));

        analyzeFile("SomeServletInitializer.class");
        doFinishAnalysis();

        verify(log, never()).warn(anyString());
    }

}