package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import org.junit.Test;

public final class An_AnnotationsAnalyzer extends AnAnalyzer {

    @Test
    public void reportsExistenceOfClass() {
        Analyzer objectUnderTest = new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("A.class"));
        assertThatClassesAreReported("A");

        objectUnderTest.doAnalysis(codeContext, getFile("B.class"));
        assertThatClassesAreReported("A", "B");
    }

    @Test
    public void reportsAnnotatedClassAsBeingUsed() {
        Analyzer objectUnderTest = new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation", "java.lang.Deprecated") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("AnnotatedClass.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));

        assertThatDependenciesAreReported("AnnotatedClass", "DeadServlet");
    }

    @Test
    public void reportsClassAnnotatedWithAnnotatedAnnotationAsBeingUsed() {
        Analyzer objectUnderTest = new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("ClassAnnotatedWithAnnotatedAnnotation.class"));

        assertThatDependenciesAreReported("ClassAnnotatedWithAnnotatedAnnotation");
    }

    @Test
    public void reportsSubClassOfClassBeingAnnotatedWithAnnotationMarkedAsInheritedAsBeingUsed() {
        Analyzer objectUnderTest = new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("SubClassOfAnnotatedClass.class"));

        assertThatDependenciesAreReported("SubClassOfAnnotatedClass");
    }

    @Test
    public void doesNotReportUnannotatedClassAsBeingUsed() {
        Analyzer objectUnderTest = new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("AnnotatedClass.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));

        assertThatDependenciesAreReported("AnnotatedClass");
    }

}
