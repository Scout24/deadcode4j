package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Arrays;

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

    @Before
    public void resetMock() {
        reset(log);
    }

    @Test
    public void logsObsoleteInterfaceEntry() {
        doFinishAnalysis();

        verify(log).warn(Matchers.contains("foo.Bar"));
    }

}