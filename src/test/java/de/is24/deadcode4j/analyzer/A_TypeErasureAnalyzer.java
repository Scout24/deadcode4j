package de.is24.deadcode4j.analyzer;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_TypeErasureAnalyzer extends AnAnalyzer {

    private TypeErasureAnalyzer objectUnderTest;

    @Before
    public void initAnalyzer() {
        objectUnderTest = new TypeErasureAnalyzer();
    }

    @Test
    public void recognizesClassTypeParameter() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/typeerasure/TypedArrayList.java"));

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList",
                "java.lang.Comparable",
                "java.math.BigDecimal",
                "java.util.Map$Entry",
                "java.util.regex.Pattern",
                "de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$InnerClass");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$InnerClass",
                "de.is24.deadcode4j.analyzer.typeerasure.PackageClass");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$SecondInnerClass",
                "de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$InnerClass$NestedInnerClass");
    }

    private void assertThatDependenciesAreReportedFor(String depender, String... dependee) {
        Map<String, Set<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat(codeDependencies, hasEntry(equalTo(depender), any(Set.class)));
        assertThat(codeDependencies.get(depender), containsInAnyOrder(dependee));
    }

}
