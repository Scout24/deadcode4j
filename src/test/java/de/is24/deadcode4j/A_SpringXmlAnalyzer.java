package de.is24.deadcode4j;

import com.google.common.collect.Iterables;
import javassist.ClassPool;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public final class A_SpringXmlAnalyzer {

    @Test
    public void shouldParseSpringFiles() {
        SpringXmlAnalyzer objectUnderTest = new SpringXmlAnalyzer();

        CodeContext codeContext = new CodeContext(getClass().getClassLoader(), mock(ClassPool.class));
        objectUnderTest.doAnalysis(codeContext, "scenarios/springbean/spring.xml");

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(Iterables.getOnlyElement(codeDependencies.values()), contains("SingleClass"));
    }

    @Test
    public void shouldNotParseNonSpringFiles() {
        SpringXmlAnalyzer objectUnderTest = new SpringXmlAnalyzer();

        CodeContext codeContext = new CodeContext(getClass().getClassLoader(), mock(ClassPool.class));
        objectUnderTest.doAnalysis(codeContext, "scenarios/nonspringxml/nospring.xml");

        assertThat("Should not have analyzed the XML file!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(0));
        assertThat("Should not have analyzed the XML file!", codeContext.getAnalyzedCode().getCodeDependencies().isEmpty());
    }

}
