package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public final class A_SuperClassAnalyzer extends AnAnalyzer {

    @Test
    public void reportsExistenceOfClasses() {
        Analyzer objectUnderTest = new SuperClassAnalyzer("junit", "java.lang.Thread") {
        };
        CodeContext codeContext = new CodeContext();

        objectUnderTest.doAnalysis(codeContext, getFile("A.class"));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("A"));

        objectUnderTest.doAnalysis(codeContext, getFile("B.class"));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("A", "B"));
    }

    @Test
    public void reportsASubClassAsLiveCode() {
        Analyzer objectUnderTest = new SuperClassAnalyzer("junit", "java.lang.Object", "java.lang.Thread") {
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("DependingClass.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassThatShouldBeLive.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have reported some dependencies!", codeDependencies.size(), is(1));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder("DependingClass", "SubClassThatShouldBeLive"));
    }

    @Test
    public void doesNotReportASubClassWithIrrelevantSuperClass() {
        Analyzer objectUnderTest = new SuperClassAnalyzer("junit", "java.lang.Thread") {
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("DependingClass.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassThatShouldBeLive.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have reported some dependencies!", codeDependencies.size(), is(1));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder("SubClassThatShouldBeLive"));
    }

}
