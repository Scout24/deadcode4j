package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_SuperClassAnalyzer extends AFinalAnalyzer<SuperClassAnalyzer> {

    @Override
    protected SuperClassAnalyzer createAnalyzer() {
        return new SuperClassAnalyzer("junit", "java.lang.Thread") {
        };
    }

    @Test
    public void reportsExistenceOfClasses() {
        objectUnderTest.doAnalysis(codeContext, getFile("A.class"));
        assertThatClassesAreReported("A");

        objectUnderTest.doAnalysis(codeContext, getFile("B.class"));
        assertThatClassesAreReported("A", "B");
    }

    @Test
    public void reportsASubClassAsLiveCode() {
        objectUnderTest = new SuperClassAnalyzer("junit", "javax.servlet.http.HttpServlet", "java.lang.Thread") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassThatShouldBeLive.class"));

        assertThatDependenciesAreReported("DeadServlet", "SubClassThatShouldBeLive");
    }

    @Test
    public void reportsASubClassOfASubClassAsLiveCode() {
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassOfSubClassThatShouldBeLive.class"));

        assertThatDependenciesAreReported("SubClassOfSubClassThatShouldBeLive");
    }

    @Test
    public void doesNotReportASubClassWithIrrelevantSuperClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassThatShouldBeLive.class"));

        assertThatDependenciesAreReported("SubClassThatShouldBeLive");
    }

}
