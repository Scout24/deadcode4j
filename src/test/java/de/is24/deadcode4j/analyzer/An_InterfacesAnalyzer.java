package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class An_InterfacesAnalyzer extends AnAnalyzer<InterfacesAnalyzer> {

    @Override
    protected InterfacesAnalyzer createAnalyzer() {
        return new InterfacesAnalyzer("junit", "java.lang.Cloneable") {
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
    public void reportsImplementingClassAsBeingUsed() {
        objectUnderTest = new InterfacesAnalyzer("junit", "java.lang.Cloneable", "java.io.Serializable") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingCloneable.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));

        assertThatDependenciesAreReported("ClassImplementingCloneable", "DeadServlet");
    }

    @Test
    public void doesNotReportNonImplementingClassAsBeingUsed() {
        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingCloneable.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));

        assertThatDependenciesAreReported("ClassImplementingCloneable");
    }

    @Test
    public void reportsSubClassImplementingClassAsBeingUsed() {
        objectUnderTest = new InterfacesAnalyzer("junit", "java.lang.Runnable") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("SubClassThatShouldBeLive.class"));

        assertThatDependenciesAreReported("SubClassThatShouldBeLive");
    }

    @Test
    public void reportsClassImplementingSubInterfaceAsBeingUsed() {
        objectUnderTest = new InterfacesAnalyzer("junit", "java.io.Serializable") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingExternalizable.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassOfClassImplementingExternalizable.class"));

        assertThatDependenciesAreReported(
                "ClassImplementingExternalizable",
                "SubClassOfClassImplementingExternalizable");
    }

}
