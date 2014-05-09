package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_SpringXmlAnalyzer extends AFinalAnalyzer<SpringXmlAnalyzer> {

    @Override
    protected SpringXmlAnalyzer createAnalyzer() {
        return new SpringXmlAnalyzer();
    }

    @Test
    public void shouldParseSpringFiles() {
        objectUnderTest.doAnalysis(codeContext, getFile("spring.xml"));

        assertThatDependenciesAreReported("SpringXmlBean");
    }

}
