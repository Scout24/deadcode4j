package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public class A_ServletContainerInitializerAnalyzer extends AnAnalyzer<ServletContainerInitializerAnalyzer> {

    @Override
    protected ServletContainerInitializerAnalyzer createAnalyzer() {
        return new ServletContainerInitializerAnalyzer("JUnit", "javax.servlet.ServletContainerInitializer") {
        };
    }

    @Test
    public void shouldRecognizeServletContainerInitializerClasses() {
        analyzeFile("de/is24/deadcode4j/analyzer/v3-metadata-missing.web.xml");
        analyzeFile("SomeServletInitializer.class");
        finishAnalysis();

        assertThatDependenciesAreReported("SomeServletInitializer");
    }

    @Test
    public void shouldRecognizeServletContainerInitializerClassesIfMetadataCompleteAttributeIsFalse() {
        analyzeFile("de/is24/deadcode4j/analyzer/v3-metadata-incomplete.web.xml");
        analyzeFile("SomeServletInitializer.class");
        finishAnalysis();

        assertThatDependenciesAreReported("SomeServletInitializer");
    }

    @Test
    public void shouldRecognizeMetadataCompleteAttribute() {
        analyzeFile("de/is24/deadcode4j/analyzer/v3-metadata-complete.web.xml");
        analyzeFile("SomeServletInitializer.class");
        finishAnalysis();

        assertThatNoDependenciesAreReported();
    }
}
