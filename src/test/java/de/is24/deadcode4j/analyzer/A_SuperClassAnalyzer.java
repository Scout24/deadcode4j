package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_SuperClassAnalyzer extends AByteCodeAnalyzer<SuperClassAnalyzer> {

    @Override
    protected SuperClassAnalyzer createAnalyzer() {
        return new SuperClassAnalyzer("junit", "java.lang.Thread") {
        };
    }

    @Test
    public void reportsASubClassAsLiveCode() {
        objectUnderTest = new SuperClassAnalyzer("junit", "javax.servlet.http.HttpServlet", "java.lang.Thread") {
        };

        analyzeFile("DeadServlet.class");
        analyzeFile("SubClassThatShouldBeLive.class");

        assertThatDependenciesAreReported("DeadServlet", "SubClassThatShouldBeLive");
    }

    @Test
    public void reportsASubClassOfASubClassAsLiveCode() {
        analyzeFile("SubClassOfSubClassThatShouldBeLive.class");

        assertThatDependenciesAreReported("SubClassOfSubClassThatShouldBeLive");
    }

    @Test
    public void doesNotReportASubClassWithIrrelevantSuperClass() {
        analyzeFile("DeadServlet.class");
        analyzeFile("SubClassThatShouldBeLive.class");

        assertThatDependenciesAreReported("SubClassThatShouldBeLive");
    }

}
