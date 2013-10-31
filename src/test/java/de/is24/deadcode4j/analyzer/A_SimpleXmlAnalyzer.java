package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_SimpleXmlAnalyzer extends AnAnalyzer {

    @Test
    public void usesTheSpecifiedDependerIdToReportDependencies() {
        final String dependerId = "junit";
        SimpleXmlAnalyzer objectUnderTest = new SimpleXmlAnalyzer(dependerId, ".xml", null) {
            {
                registerClassElement("elementWithClass");
            }
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(codeDependencies.keySet(), contains(dependerId));
    }

    @Test
    public void ignoresFilesWithNonMatchingRootElement() {
        SimpleXmlAnalyzer objectUnderTest = new SimpleXmlAnalyzer("junit", ".xml", "acme") {
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/empty.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should NOT have analyzed the XML file!", codeDependencies.size(), is(0));
    }

    @Test
    public void reportsTheClassFoundForTheRegisteredElement() {
        SimpleXmlAnalyzer objectUnderTest = new SimpleXmlAnalyzer("junit", ".xml", null) {
            {
                registerClassElement("elementWithClass");
            }
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), contains("de.is24.deadcode4j.ClassInElement"));
    }

    @Test
    public void reportsTheClassFoundForTheRegisteredAttribute() {
        SimpleXmlAnalyzer objectUnderTest = new SimpleXmlAnalyzer("junit", ".xml", null) {
            {
                registerClassAttribute("element", "attributeWithClass");
            }
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), contains("de.is24.deadcode4j.ClassInAttribute"));
    }

    @Test
    public void reportsMultipleFoundClasses() {
        SimpleXmlAnalyzer objectUnderTest = new SimpleXmlAnalyzer("junit", ".xml", null) {
            {
                registerClassElement("anotherElementWithClass");
            }
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("de.is24.deadcode4j.FirstClassInAnotherElement",
                        "de.is24.deadcode4j.SecondClassInAnotherElement"));
    }

    @Test
    public void reportsClassesForDifferentRegistrations() {
        SimpleXmlAnalyzer objectUnderTest = new SimpleXmlAnalyzer("junit", ".xml", null) {
            {
                registerClassElement("elementWithClass");
                registerClassElement("anotherElementWithClass");
                registerClassAttribute("element", "attributeWithClass");
            }
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("de.is24.deadcode4j.ClassInElement", "de.is24.deadcode4j.FirstClassInAnotherElement",
                        "de.is24.deadcode4j.SecondClassInAnotherElement", "de.is24.deadcode4j.ClassInAttribute"));
    }

    @Test
    public void parsesXmlFilesUsingNamespacePrefixes() {
        SimpleXmlAnalyzer objectUnderTest = new SimpleXmlAnalyzer("junit", ".xml", "root") {
            {
                registerClassElement("elementWithClass");
                registerClassElement("anotherElementWithClass");
                registerClassAttribute("element", "attributeWithClass");
            }
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/prefixed.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("de.is24.deadcode4j.ClassInElement", "de.is24.deadcode4j.FirstClassInAnotherElement",
                        "de.is24.deadcode4j.SecondClassInAnotherElement", "de.is24.deadcode4j.ClassInAttribute"));
    }

}
