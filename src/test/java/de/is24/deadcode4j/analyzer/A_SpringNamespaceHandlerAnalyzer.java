package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public final class A_SpringNamespaceHandlerAnalyzer extends AnAnalyzer {

    @Test
    public void shouldRecognizeDefinedNamespaceHandlers() {
        Analyzer objectUnderTest = new SpringNamespaceHandlerAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("META-INF/spring.handlers"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        Iterable<String> allReportedClasses = concat(codeDependencies.values());
        assertThat(allReportedClasses, containsInAnyOrder("CustomNamespaceHandler", "AnotherNamespaceHandler"));
    }

}
