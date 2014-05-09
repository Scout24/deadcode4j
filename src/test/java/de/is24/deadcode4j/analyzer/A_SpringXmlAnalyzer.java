package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_SpringXmlAnalyzer extends AnAnalyzer {

    @Test
    public void shouldParseSpringFiles() {
        SpringXmlAnalyzer objectUnderTest = new SpringXmlAnalyzer();

        objectUnderTest.doAnalysis(codeContext, getFile("spring.xml"));

        assertThatDependenciesAreReported("SpringXmlBean");
    }

}
