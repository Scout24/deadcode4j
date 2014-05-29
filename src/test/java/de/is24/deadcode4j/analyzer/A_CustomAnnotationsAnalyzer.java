package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Test;
import org.mockito.Matchers;

import static java.util.Collections.singleton;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public final class A_CustomAnnotationsAnalyzer extends AnAnalyzer<CustomAnnotationsAnalyzer> {

    private Log log;

    @Override
    public LoggingRule enableLogging() {
        log = LoggingRule.createMock();
        return new LoggingRule(log);
    }

    @Override
    protected CustomAnnotationsAnalyzer createAnalyzer() {
        return new CustomAnnotationsAnalyzer(singleton("foo.Bar"));
    }

    @After
    public void resetMock() {
        reset(log);
    }

    @Test
    public void logsObsoleteInterfaceEntry() {
        analyzeFile("AnnotatedClass.class");
        doFinishAnalysis();

        verify(log).warn(Matchers.contains("foo.Bar"));
    }

    @Test
    public void doesNotLogInterfaceEntryFoundInClassPath() {
        this.objectUnderTest = new CustomAnnotationsAnalyzer(singleton("de.is24.deadcode4j.junit.Annotation"));

        analyzeFile("AnnotatedClass.class");
        doFinishAnalysis();

        verify(log, never()).warn(anyString());
    }

}