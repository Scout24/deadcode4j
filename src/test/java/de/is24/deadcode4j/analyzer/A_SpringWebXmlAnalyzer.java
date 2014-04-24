package de.is24.deadcode4j.analyzer;

import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;

public final class A_SpringWebXmlAnalyzer extends AnAnalyzer {

    @Test
    public void shouldParseWebXmlFiles() {
        SpringWebXmlAnalyzer objectUnderTest = new SpringWebXmlAnalyzer();

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/spring.web.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the web.xml file!", codeDependencies.size(), greaterThan(1));

        Iterable<String> allReportedClasses = concat(codeDependencies.values());
        assertThat(allReportedClasses, containsInAnyOrder("servlet.contextClass", "servlet.initializerClass",
                "root.contextClass", "root.initializerClass", "root.secondInitializerClass"));
    }

}
