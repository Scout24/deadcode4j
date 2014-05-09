package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_CustomXmlAnalyzer extends AnAnalyzer {

    @Test(expected = IllegalArgumentException.class)
    public void failsIfInvalidXPathIsSpecified() {
        CustomXmlAnalyzer objectUnderTest = new CustomXmlAnalyzer("junit", ".xml", null);
        objectUnderTest.registerXPath("elementWithClass");
    }

    @Test
    public void selectsTheTextOfTheSpecifiedNode() {
        CustomXmlAnalyzer objectUnderTest = new CustomXmlAnalyzer("junit", ".xml", null);
        objectUnderTest.registerXPath("elementWithClass/text()");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInElement");
    }

    @Test
    public void selectsTheTextOfTheRestrictedNode() {
        CustomXmlAnalyzer objectUnderTest = new CustomXmlAnalyzer("junit", ".xml", null);
        objectUnderTest.registerXPath("restrictedElement[@locked='false']/text()");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        assertThatDependenciesAreReported("de.is24.deadcode4j.UnlockedClassInElement");
    }

    @Test
    public void selectsTheAttributeOfTheSpecifiedNode() {
        CustomXmlAnalyzer objectUnderTest = new CustomXmlAnalyzer("junit", ".xml", null);
        objectUnderTest.registerXPath("element/@attributeWithClass");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInAttribute");
    }

    @Test
    public void selectsTheAttributeOfTheRestrictedNode() {
        CustomXmlAnalyzer objectUnderTest = new CustomXmlAnalyzer("junit", ".xml", null);
        objectUnderTest.registerXPath("restrictedElement[@locked='false']/@attributeWithClass");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        assertThatDependenciesAreReported("de.is24.deadcode4j.UnlockedClassInAttribute");
    }

}
