package de.is24.deadcode4j.analyzer;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public final class An_ExtendedXmlAnalyzer extends AnAnalyzer<ExtendedXmlAnalyzer> {

    @Override
    protected ExtendedXmlAnalyzer createAnalyzer() {
        return new ExtendedXmlAnalyzer("junit", ".xml") {
        };
    }

    @Test
    public void usesTheSpecifiedDependerIdToReportDependencies() {
        objectUnderTest.anyElementNamed("elementWithClass").register();

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        Map<String, ? extends Iterable<String>> codeDependencies = analysisContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(codeDependencies.keySet(), contains("junit"));
    }

    @Test
    public void reportsTheClassFoundForTheRegisteredElement() {
        objectUnderTest.anyElementNamed("elementWithClass").register();

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInElement");
    }

    @Test
    public void reportsTheClassFoundForElementsInVariousPaths() {
        objectUnderTest.anyElementNamed("nestedElementWithClass").register();

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatDependenciesAreReported("de.is24.deadcode4j.ClassInElement",
                "de.is24.deadcode4j.ClassInNestedElement");
    }

    @Test
    public void reportsNothingIfElementHasNoText() {
        objectUnderTest.anyElementNamed("emptyElement").register();

        analyzeFile("de/is24/deadcode4j/analyzer/some.xml");

        assertThatNoDependenciesAreReported();
    }

}