package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class An_InterfacesAnalyzer extends AByteCodeAnalyzer<InterfacesAnalyzer> {

    @Override
    protected InterfacesAnalyzer createAnalyzer() {
        return new InterfacesAnalyzer("junit", "java.lang.Cloneable") {
        };
    }

    @Test
    public void reportsImplementingClassAsBeingUsed() {
        objectUnderTest = new InterfacesAnalyzer("junit", "java.lang.Cloneable", "java.io.Serializable") {
        };

        analyzeFile("ClassImplementingCloneable.class");
        analyzeFile("DeadServlet.class");

        assertThatDependenciesAreReported("ClassImplementingCloneable", "DeadServlet");
    }

    @Test
    public void doesNotReportNonImplementingClassAsBeingUsed() {
        analyzeFile("ClassImplementingCloneable.class");
        analyzeFile("DeadServlet.class");

        assertThatDependenciesAreReported("ClassImplementingCloneable");
    }

    @Test
    public void reportsSubClassImplementingClassAsBeingUsed() {
        objectUnderTest = new InterfacesAnalyzer("junit", "java.lang.Runnable") {
        };

        analyzeFile("SubClassThatShouldBeLive.class");

        assertThatDependenciesAreReported("SubClassThatShouldBeLive");
    }

    @Test
    public void reportsClassImplementingSubInterfaceAsBeingUsed() {
        objectUnderTest = new InterfacesAnalyzer("junit", "java.io.Serializable") {
        };

        analyzeFile("ClassImplementingExternalizable.class");
        analyzeFile("SubClassOfClassImplementingExternalizable.class");

        assertThatDependenciesAreReported(
                "ClassImplementingExternalizable",
                "SubClassOfClassImplementingExternalizable");
    }

}
