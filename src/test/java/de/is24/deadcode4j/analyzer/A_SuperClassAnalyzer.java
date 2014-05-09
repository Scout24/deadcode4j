package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import org.junit.Test;

public final class A_SuperClassAnalyzer extends AnAnalyzer {

    @Test
    public void reportsExistenceOfClasses() {
        Analyzer objectUnderTest = new SuperClassAnalyzer("junit", "java.lang.Thread") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("A.class"));
        assertThatClassesAreReported("A");

        objectUnderTest.doAnalysis(codeContext, getFile("B.class"));
        assertThatClassesAreReported("A", "B");
    }

    @Test
    public void reportsASubClassAsLiveCode() {
        Analyzer objectUnderTest = new SuperClassAnalyzer("junit", "javax.servlet.http.HttpServlet", "java.lang.Thread") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassThatShouldBeLive.class"));

        assertThatDependenciesAreReported("DeadServlet", "SubClassThatShouldBeLive");
    }

    @Test
    public void reportsASubClassOfASubClassAsLiveCode() {
        Analyzer objectUnderTest = new SuperClassAnalyzer("junit", "java.lang.Thread") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("SubClassOfSubClassThatShouldBeLive.class"));

        assertThatDependenciesAreReported("SubClassOfSubClassThatShouldBeLive");
    }

    @Test
    public void doesNotReportASubClassWithIrrelevantSuperClass() {
        Analyzer objectUnderTest = new SuperClassAnalyzer("junit", "java.lang.Thread") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassThatShouldBeLive.class"));

        assertThatDependenciesAreReported("SubClassThatShouldBeLive");
    }

}
