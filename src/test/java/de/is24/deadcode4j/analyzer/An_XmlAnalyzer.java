package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import javassist.ClassPool;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public final class An_XmlAnalyzer extends AnAnalyzer {

    @Test
    public void parsesTheSpecifiedFile() {
        XmlAnalyzer objectUnderTest = new XmlAnalyzer("junit", ".xml", null) {
        };

        CodeContext codeContext = createCodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/empty.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
    }

    @Test
    public void usesTheSpecifiedDependerIdToReportDependencies() {
        final String dependerId = "junit";
        XmlAnalyzer objectUnderTest = new XmlAnalyzer(dependerId, ".xml", null) {
        };

        CodeContext codeContext = createCodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/empty.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(codeDependencies.keySet(), contains(dependerId));
    }

    @Test
    public void ignoresFilesWithNonMatchingEnding() {
        XmlAnalyzer objectUnderTest = new XmlAnalyzer("junit", ".acme", null) {
        };

        CodeContext codeContext = createCodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/empty.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should NOT have analyzed the XML file!", codeDependencies.size(), is(0));
    }

    @Test
    public void ignoresFilesWithNonMatchingRootElement() {
        XmlAnalyzer objectUnderTest = new XmlAnalyzer("junit", ".xml", "acme") {
        };

        CodeContext codeContext = createCodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/empty.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should NOT have analyzed the XML file!", codeDependencies.size(), is(0));
    }

    @Test
    public void reportsTheClassFoundForTheRegisteredElement() {
        XmlAnalyzer objectUnderTest = new XmlAnalyzer("junit", ".xml", null) {
            {
                registerClassElement("elementWithClass");
            }
        };

        CodeContext codeContext = createCodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), contains("de.is24.deadcode4j.ClassInElement"));
    }

    @Test
    public void reportsTheClassFoundForTheRegisteredAttribute() {
        XmlAnalyzer objectUnderTest = new XmlAnalyzer("junit", ".xml", null) {
            {
                registerClassAttribute("element", "attributeWithClass");
            }
        };

        CodeContext codeContext = createCodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), contains("de.is24.deadcode4j.ClassInAttribute"));
    }

    @Test
    public void reportsMultipleFoundClasses() {
        XmlAnalyzer objectUnderTest = new XmlAnalyzer("junit", ".xml", null) {
            {
                registerClassElement("anotherElementWithClass");
            }
        };

        CodeContext codeContext = createCodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("de.is24.deadcode4j.FirstClassInAnotherElement",
                        "de.is24.deadcode4j.SecondClassInAnotherElement"));
    }

    @Test
    public void reportsClassesForDifferentRegistrations() {
        XmlAnalyzer objectUnderTest = new XmlAnalyzer("junit", ".xml", null) {
            {
                registerClassElement("elementWithClass");
                registerClassElement("anotherElementWithClass");
                registerClassAttribute("element", "attributeWithClass");
            }
        };

        CodeContext codeContext = createCodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/some.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("de.is24.deadcode4j.ClassInElement", "de.is24.deadcode4j.FirstClassInAnotherElement",
                        "de.is24.deadcode4j.SecondClassInAnotherElement", "de.is24.deadcode4j.ClassInAttribute"));
    }

    @Test
    public void parsesXmlFilesUsingNamespacePrefixes() {
        XmlAnalyzer objectUnderTest = new XmlAnalyzer("junit", ".xml", "root") {
            {
                registerClassElement("elementWithClass");
                registerClassElement("anotherElementWithClass");
                registerClassAttribute("element", "attributeWithClass");
            }
        };

        CodeContext codeContext = createCodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/prefixed.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("de.is24.deadcode4j.ClassInElement", "de.is24.deadcode4j.FirstClassInAnotherElement",
                        "de.is24.deadcode4j.SecondClassInAnotherElement", "de.is24.deadcode4j.ClassInAttribute"));
    }

    private CodeContext createCodeContext() {
        return new CodeContext(getClass().getClassLoader(), mock(ClassPool.class));
    }

}
