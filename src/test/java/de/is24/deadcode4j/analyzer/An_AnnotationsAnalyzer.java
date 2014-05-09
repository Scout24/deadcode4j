package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public final class An_AnnotationsAnalyzer extends AnAnalyzer {

    @Test
    public void reportsExistenceOfClass() {
        Analyzer objectUnderTest = new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("A.class"));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("A"));

        objectUnderTest.doAnalysis(codeContext, getFile("B.class"));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("A", "B"));
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
