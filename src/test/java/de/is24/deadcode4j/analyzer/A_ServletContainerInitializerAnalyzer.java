package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class A_ServletContainerInitializerAnalyzer extends AnAnalyzer {

    @Test
    public void shouldRecognizeServletContainerInitializerClasses() {
        Analyzer objectUnderTest = new ServletContainerInitializerAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/v3-metadata-missing.web.xml"));
        objectUnderTest.doAnalysis(codeContext, getFile("SomeServletInitializer.class"));
        objectUnderTest.finishAnalysis(codeContext);

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        Iterable<String> allReportedClasses = concat(codeDependencies.values());
        assertThat(allReportedClasses, containsInAnyOrder("SomeServletInitializer"));
    }

    @Test
    public void shouldRecognizeServletContainerInitializerClassesIfMetadataCompleteAttributeIsFalse() {
        Analyzer objectUnderTest = new ServletContainerInitializerAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/v3-metadata-incomplete.web.xml"));
        objectUnderTest.doAnalysis(codeContext, getFile("SomeServletInitializer.class"));
        objectUnderTest.finishAnalysis(codeContext);

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        Iterable<String> allReportedClasses = concat(codeDependencies.values());
        assertThat(allReportedClasses, containsInAnyOrder("SomeServletInitializer"));
    }

    @Test
    public void shouldRecognizeMetadataCompleteAttribute() {
        Analyzer objectUnderTest = new ServletContainerInitializerAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/v3-metadata-complete.web.xml"));
        objectUnderTest.doAnalysis(codeContext, getFile("SomeServletInitializer.class"));
        objectUnderTest.finishAnalysis(codeContext);

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        Iterable<String> allReportedClasses = concat(codeDependencies.values());
        assertThat(allReportedClasses, is(emptyIterable()));
    }

}
