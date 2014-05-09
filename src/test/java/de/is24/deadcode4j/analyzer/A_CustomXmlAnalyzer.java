package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_CustomXmlAnalyzer extends AnAnalyzer<CustomXmlAnalyzer> {

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

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInElement");
    }

    @Test
    public void selectsTheTextOfTheRestrictedNode() {
        objectUnderTest.registerXPath("restrictedElement[@locked='false']/text()");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        assertThatDependenciesAreReported("de.is24.deadcode4j.UnlockedClassInElement");
    }

    @Test
    public void selectsTheAttributeOfTheSpecifiedNode() {
        objectUnderTest.registerXPath("element/@attributeWithClass");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInAttribute");
    }

    @Test
    public void selectsTheAttributeOfTheRestrictedNode() {
        objectUnderTest.registerXPath("restrictedElement[@locked='false']/@attributeWithClass");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        assertThatDependenciesAreReported("de.is24.deadcode4j.UnlockedClassInAttribute");
    }

}
