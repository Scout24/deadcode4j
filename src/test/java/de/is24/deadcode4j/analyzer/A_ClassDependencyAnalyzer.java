package de.is24.deadcode4j.analyzer;

import org.junit.Test;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assume.assumeThat;

public final class A_ClassDependencyAnalyzer extends AFinalAnalyzer<ClassDependencyAnalyzer> {

    @Override
    protected ClassDependencyAnalyzer createAnalyzer() {
        return new ClassDependencyAnalyzer();
    }

    @Test
    public void reportsExistenceOfClassAndReportsItsDependencies() {
        objectUnderTest.doAnalysis(codeContext, getFile("SingleClass.class"));

        assertThatClassesAreReported("SingleClass");
        assertThatDependenciesAreReportedFor("SingleClass", "java.lang.Object");
    }

    @Test
    public void reportsTheDependencyOfAClassToAnother() {
        objectUnderTest.doAnalysis(codeContext, getFile("DependingClass.class"));

        assertThatClassesAreReported("DependingClass");
        assertThatDependenciesAreReportedFor("DependingClass",
                "IndependentClass",
                "java.lang.Object");
    }

    @Test
    public void recognizesDependenciesToInnerClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/classdependency/ClassWithInnerClasses.class"));

        assertThatClassesAreReported("de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses");
        Iterable<String> allDependencies = concat(codeContext.getAnalyzedCode().getCodeDependencies().values());
        assumeThat("Inner classes are only defined, but not used by the parent class!", allDependencies, containsInAnyOrder("java.lang.Object", "de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses$UsedStaticInnerClass"));
    }

    @Test
    public void recognizesDependencyOfInnerClassToParentClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/classdependency/ClassWithInnerClasses$UnusedInnerClass.class"));

        assertThatClassesAreReported("de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses$UnusedInnerClass");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses$UnusedInnerClass",
                "de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses",
                "java.lang.Object");
    }

    @Test
    public void recognizesNoDependencyOfStaticInnerClassToParentClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/classdependency/ClassWithInnerClasses$UnusedStaticInnerClass.class"));

        assertThatClassesAreReported("de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses$UnusedStaticInnerClass");
        Iterable<String> allDependencies = concat(codeContext.getAnalyzedCode().getCodeDependencies().values());
        assumeThat("Although technically correct (to keep up the namespace, the parent class is required), the static inner class does not access the parent class!", allDependencies, containsInAnyOrder("java.lang.Object"));
    }

}
