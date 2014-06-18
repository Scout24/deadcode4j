package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public final class A_CustomXmlAnalyzer extends AnAnalyzer<CustomXmlAnalyzer> {

    private Log logMock;

    @Override
    public LoggingRule enableLogging() {
        logMock = LoggingRule.createMock();
        return new LoggingRule(logMock);
    }

    @After
    public void resetMock() {
        reset(logMock);
    }

    @Override
    protected CustomXmlAnalyzer createAnalyzer() {
        return new CustomXmlAnalyzer("junit", ".xml", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfInvalidXPathIsSpecified() {
        objectUnderTest.registerXPath("elementWithClass");
    }

    @Test
    public void selectsTheTextOfTheSpecifiedNode() {
        objectUnderTest.registerXPath("elementWithClass/text()");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInElement");
    }

    @Test
    public void selectsTheTextOfTheRestrictedNode() {
        objectUnderTest.registerXPath("restrictedElement[@locked='false']/text()");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.UnlockedClassInElement");
    }

    @Test
    public void selectsTheAttributeOfTheSpecifiedNode() {
        objectUnderTest.registerXPath("element/@attributeWithClass");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInAttribute");
    }

    @Test
    public void selectsTheAttributeOfTheRestrictedNode() {
        objectUnderTest.registerXPath("restrictedElement[@locked='false']/@attributeWithClass");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.UnlockedClassInAttribute");
    }

    @Test
    public void issuesWarningIfNothingIsFound() {
        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");
        doFinishAnalysis();

        verify(logMock).warn(Matchers.contains(CustomXmlAnalyzer.class.getSimpleName()));
    }

    @Test
    public void issuesNoWarningIfSomethingIsFound() {
        objectUnderTest.registerXPath("elementWithClass/text()");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");
        doFinishAnalysis();

        verify(logMock, never()).warn(anyString());
    }

}
