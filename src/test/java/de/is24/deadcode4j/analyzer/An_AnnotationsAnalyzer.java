package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class An_AnnotationsAnalyzer extends AByteCodeAnalyzer<AnnotationsAnalyzer> {

    @Override
    protected AnnotationsAnalyzer createAnalyzer() {
        return new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation") {
        };
    }

    @Test
    public void reportsAnnotatedClassAsBeingUsed() {
        objectUnderTest = new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation", "java.lang.Deprecated") {
        };

        analyzeFile("AnnotatedClass.class");
        analyzeFile("DeadServlet.class");

        assertThatDependenciesAreReported("AnnotatedClass", "DeadServlet");
    }

    @Test
    public void reportsClassAnnotatedWithAnnotatedAnnotationAsBeingUsed() {
        analyzeFile("ClassAnnotatedWithAnnotatedAnnotation.class");

        assertThatDependenciesAreReported("ClassAnnotatedWithAnnotatedAnnotation");
    }

    @Test
    public void reportsSubClassOfClassBeingAnnotatedWithAnnotationMarkedAsInheritedAsBeingUsed() {
        analyzeFile("SubClassOfAnnotatedClass.class");

        assertThatDependenciesAreReported("SubClassOfAnnotatedClass");
    }

    @Test
    public void doesNotReportUnannotatedClassAsBeingUsed() {
        analyzeFile("AnnotatedClass.class");
        analyzeFile("DeadServlet.class");

        assertThatDependenciesAreReported("AnnotatedClass");
    }

}
