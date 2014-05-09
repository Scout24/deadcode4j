package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class An_AnnotationsAnalyzer extends AFinalAnalyzer<AnnotationsAnalyzer> {

    @Override
    protected AnnotationsAnalyzer createAnalyzer() {
        return new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation") {
        };
    }

    @Test
    public void reportsExistenceOfClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("A.class"));
        assertThatClassesAreReported("A");

        objectUnderTest.doAnalysis(codeContext, getFile("B.class"));
        assertThatClassesAreReported("A", "B");
    }

    @Test
    public void reportsAnnotatedClassAsBeingUsed() {
        objectUnderTest = new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation", "java.lang.Deprecated") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("AnnotatedClass.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));

        assertThatDependenciesAreReported("AnnotatedClass", "DeadServlet");
    }

    @Test
    public void reportsClassAnnotatedWithAnnotatedAnnotationAsBeingUsed() {
        objectUnderTest.doAnalysis(codeContext, getFile("ClassAnnotatedWithAnnotatedAnnotation.class"));

        assertThatDependenciesAreReported("ClassAnnotatedWithAnnotatedAnnotation");
    }

    @Test
    public void reportsSubClassOfClassBeingAnnotatedWithAnnotationMarkedAsInheritedAsBeingUsed() {
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassOfAnnotatedClass.class"));

        assertThatDependenciesAreReported("SubClassOfAnnotatedClass");
    }

    @Test
    public void doesNotReportUnannotatedClassAsBeingUsed() {
        objectUnderTest.doAnalysis(codeContext, getFile("AnnotatedClass.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));

        assertThatDependenciesAreReported("AnnotatedClass");
    }

}
