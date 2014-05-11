package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_SpringNamespaceHandlerAnalyzer extends AnAnalyzer<SpringNamespaceHandlerAnalyzer> {

    @Override
    protected SpringNamespaceHandlerAnalyzer createAnalyzer() {
        return new SpringNamespaceHandlerAnalyzer();
    }

    @Test
    public void shouldRecognizeDefinedNamespaceHandlers() {
        analyzeFile("META-INF/spring.handlers");

        assertThatDependenciesAreReported("CustomNamespaceHandler", "AnotherNamespaceHandler");
    }

}
