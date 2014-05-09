package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import org.junit.Test;

public final class A_SpringNamespaceHandlerAnalyzer extends AnAnalyzer {

    @Test
    public void shouldRecognizeDefinedNamespaceHandlers() {
        Analyzer objectUnderTest = new SpringNamespaceHandlerAnalyzer();

        objectUnderTest.doAnalysis(codeContext, getFile("META-INF/spring.handlers"));

        assertThatDependenciesAreReported("CustomNamespaceHandler", "AnotherNamespaceHandler");
    }

}
