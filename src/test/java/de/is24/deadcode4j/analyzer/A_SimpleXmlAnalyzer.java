package de.is24.deadcode4j.analyzer;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public final class A_SimpleXmlAnalyzer extends AnAnalyzer<SimpleXmlAnalyzer> {

    @Override
    protected SimpleXmlAnalyzer createAnalyzer() {
        return new SimpleXmlAnalyzer("junit", ".xml", null) {
        };
    }

    @Test
    public void usesTheSpecifiedDependerIdToReportDependencies() {
        objectUnderTest.registerClassElement("elementWithClass");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        Map<String, ? extends Iterable<String>> codeDependencies = analysisContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(codeDependencies.keySet(), contains("junit"));
    }

    @Test
    public void ignoresFilesWithNonMatchingRootElement() {
        objectUnderTest = new SimpleXmlAnalyzer("junit", ".xml", "acme") {
        };
        objectUnderTest.registerClassElement("elementWithClass");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatNoDependenciesAreReported();
    }

    @Test
    public void reportsTheClassFoundForTheRegisteredElement() {
        objectUnderTest.registerClassElement("elementWithClass");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInElement");
    }

    @Test
    public void reportsTheClassFound_ForTheRegisteredElement_HavingASpecificAttributeValue() {
        objectUnderTest.registerClassElement("restrictedElement").withAttributeValue("locked", "false");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.UnlockedClassInElement");
    }

    @Test
    public void reportsTheClassFoundForTheRegisteredAttribute() {
        objectUnderTest.registerClassAttribute("element", "attributeWithClass");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInAttribute");
    }

    @Test
    public void reportsTheClassFound_ForTheRegisteredAttribute_HavingASpecificAttributeValue() {
        objectUnderTest.registerClassAttribute("restrictedElement", "attributeWithClass").withAttributeValue("locked", "false");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.UnlockedClassInAttribute");
    }

    @Test
    public void reportsTheClassesFound_ForTheRegisteredElementAndForTheRegisteredAttribute_BothHavingASpecificAttributeValue() {
        objectUnderTest.registerClassElement("restrictedElement").withAttributeValue("locked", "false");
        objectUnderTest.registerClassAttribute("restrictedElement", "attributeWithClass").withAttributeValue("locked", "false");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported(
                "de.is24.deadcode4j.UnlockedClassInAttribute",
                "de.is24.deadcode4j.UnlockedClassInElement");
    }

    @Test
    public void reportsMultipleFoundClasses() {
        objectUnderTest.registerClassElement("anotherElementWithClass");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported(
                "de.is24.deadcode4j.FirstClassInAnotherElement",
                "de.is24.deadcode4j.SecondClassInAnotherElement");
    }

    @Test
    public void reportsClassesForDifferentRegistrations() {
        objectUnderTest.registerClassElement("elementWithClass");
        objectUnderTest.registerClassElement("anotherElementWithClass");
        objectUnderTest.registerClassAttribute("element", "attributeWithClass");

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported(
                "de.is24.deadcode4j.ClassInElement",
                "de.is24.deadcode4j.FirstClassInAnotherElement",
                "de.is24.deadcode4j.SecondClassInAnotherElement",
                "de.is24.deadcode4j.ClassInAttribute");
    }

    @Test
    public void parsesXmlFilesUsingNamespacePrefixes() {
        objectUnderTest.registerClassElement("elementWithClass");
        objectUnderTest.registerClassElement("anotherElementWithClass");
        objectUnderTest.registerClassAttribute("element", "attributeWithClass");

        analyzeFile("de/is24/deadcode4j/analyzer/prefixed.xml");

        assertThatDependenciesAreReported(
                "de.is24.deadcode4j.ClassInElement",
                "de.is24.deadcode4j.FirstClassInAnotherElement",
                "de.is24.deadcode4j.SecondClassInAnotherElement",
                "de.is24.deadcode4j.ClassInAttribute");
    }

}
