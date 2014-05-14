package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public class A_ServletContainerInitializerAnalyzer extends AnAnalyzer<ServletContainerInitializerAnalyzer> {

    @Override
    protected ServletContainerInitializerAnalyzer createAnalyzer() {
        return new ServletContainerInitializerAnalyzer("JUnit", "de.is24.deadcode4j.junit.SomeInterface") {
        };
    }

    @Test
    public void shouldRecognizeServletContainerInitializerClasses() {
        analyzeFile("de/is24/deadcode4j/analyzer/v3-metadata-missing.web.xml");
        analyzeFile("SomeServletInitializer.class");

        assertThatDependenciesAreReported("SomeServletInitializer");
    }

    @Test
    public void shouldRecognizeServletContainerInitializerClassesIfMetadataCompleteAttributeIsFalse() {
        analyzeFile("de/is24/deadcode4j/analyzer/v3-metadata-incomplete.web.xml");
        analyzeFile("SomeServletInitializer.class");

        assertThatDependenciesAreReported("SomeServletInitializer");
    }

    @Test
    public void shouldRecognizeMetadataCompleteAttribute() {
        analyzeFile("de/is24/deadcode4j/analyzer/v3-metadata-complete.web.xml");
        analyzeFile("SomeServletInitializer.class");

        assertThatNoDependenciesAreReported();
    }
}
