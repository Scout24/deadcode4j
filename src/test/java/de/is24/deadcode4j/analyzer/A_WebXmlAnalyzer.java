package de.is24.deadcode4j.analyzer;

import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public final class A_WebXmlAnalyzer extends AnAnalyzer {

    @Test
    public void shouldParseWebXmlFiles() {
        WebXmlAnalyzer objectUnderTest = new WebXmlAnalyzer();

        objectUnderTest.doAnalysis(codeContext, getFile("web.xml"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the XML file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("WebAppListener", "WebAppFilter", "WebAppServlet"));
    }

}
