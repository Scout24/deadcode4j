package de.is24.deadcode4j.analyzer;

import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), contains("de.is24.deadcode4j.ClassInElement"));
    }

    @Test
    public void selectsTheTextOfTheRestrictedNode() {
        CustomXmlAnalyzer objectUnderTest = new CustomXmlAnalyzer("junit", ".xml", null);
        objectUnderTest.registerXPath("restrictedElement[@locked='false']/text()");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder("de.is24.deadcode4j.UnlockedClassInElement"));
    }

    @Test
    public void selectsTheAttributeOfTheSpecifiedNode() {
        CustomXmlAnalyzer objectUnderTest = new CustomXmlAnalyzer("junit", ".xml", null);
        objectUnderTest.registerXPath("element/@attributeWithClass");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), contains("de.is24.deadcode4j.ClassInAttribute"));
    }

    @Test
    public void selectsTheAttributeOfTheRestrictedNode() {
        CustomXmlAnalyzer objectUnderTest = new CustomXmlAnalyzer("junit", ".xml", null);
        objectUnderTest.registerXPath("restrictedElement[@locked='false']/@attributeWithClass");

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder("de.is24.deadcode4j.UnlockedClassInAttribute"));
    }

}
