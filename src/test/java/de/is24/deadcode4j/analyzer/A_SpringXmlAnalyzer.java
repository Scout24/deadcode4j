package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_SpringXmlAnalyzer extends AnAnalyzer<SpringXmlAnalyzer> {

    @Override
    protected SpringXmlAnalyzer createAnalyzer() {
        return new SpringXmlAnalyzer();
    }

    @Test
    public void shouldParseSpringFiles() {
        analyzeFile("spring.xml");

        assertThatDependenciesAreReported(
                "SpringXmlBean",
                "de.is24.deadcode4j.MapFactory",
                "java.lang.System",
                "java.util.regex.Pattern",
                "org.springframework.beans.factory.config.MethodInvokingFactoryBean");
    }

}
