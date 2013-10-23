package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

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

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), contains("de.is24.deadcode4j.ClassInElement"));
    }

    @Test
    public void selectsTheAttributeOfTheSpecifiedNode() {
        CustomXmlAnalyzer objectUnderTest = new CustomXmlAnalyzer("junit", ".xml", null);
        objectUnderTest.registerXPath("element/@attributeWithClass");

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), contains("de.is24.deadcode4j.ClassInAttribute"));
    }

}
