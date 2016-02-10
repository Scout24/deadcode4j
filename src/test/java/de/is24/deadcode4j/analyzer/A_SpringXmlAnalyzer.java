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
                // regular beans
                "SpringXmlBean",
                // MethodInvokingFactoryBean,
                "org.springframework.beans.factory.config.MethodInvokingFactoryBean",
                "de.is24.deadcode4j.mifb.Factory",
                "de.is24.deadcode4j.mifb.One",
                "de.is24.deadcode4j.mifb.Two",
                "de.is24.deadcode4j.mifb.Three",
                "de.is24.deadcode4j.mifb.Four",
                // CXF
                "de.is24.deadcode4j.jaxws.One",
                "de.is24.deadcode4j.jaxws.Two",
                "de.is24.deadcode4j.jaxws.Three",
                // JobDetailBean
                "org.springframework.scheduling.quartz.JobDetailBean",
                "de.is24.deadcode4j.jdb.Factory",
                "de.is24.deadcode4j.jdb.One",
                "de.is24.deadcode4j.jdb.Two",
                // JobDetailFactoryBean
                "org.springframework.scheduling.quartz.JobDetailFactoryBean",
                "de.is24.deadcode4j.jdfb.Factory",
                "de.is24.deadcode4j.jdfb.One",
                "de.is24.deadcode4j.jdfb.Two",
                // view resolver
                "org.springframework.web.servlet.view.UrlBasedViewResolver",
                "de.is24.deadcode4j.vr.ViewResolver",
                "de.is24.deadcode4j.vr.One",
                "de.is24.deadcode4j.vr.Two",
                "de.is24.deadcode4j.vr.Three"
        );
    }

}
