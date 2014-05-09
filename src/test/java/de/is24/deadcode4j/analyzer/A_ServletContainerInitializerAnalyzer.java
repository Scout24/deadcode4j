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
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/v3-metadata-missing.web.xml"));
        objectUnderTest.doAnalysis(codeContext, getFile("SomeServletInitializer.class"));
        objectUnderTest.finishAnalysis(codeContext);

        assertThatDependenciesAreReported("SomeServletInitializer");
    }

    @Test
    public void shouldRecognizeServletContainerInitializerClassesIfMetadataCompleteAttributeIsFalse() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/v3-metadata-incomplete.web.xml"));
        objectUnderTest.doAnalysis(codeContext, getFile("SomeServletInitializer.class"));
        objectUnderTest.finishAnalysis(codeContext);

        assertThatDependenciesAreReported("SomeServletInitializer");
    }

    @Test
    public void shouldRecognizeMetadataCompleteAttribute() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/v3-metadata-complete.web.xml"));
        objectUnderTest.doAnalysis(codeContext, getFile("SomeServletInitializer.class"));
        objectUnderTest.finishAnalysis(codeContext);

        assertThatNoDependenciesAreReported();
    }
}
