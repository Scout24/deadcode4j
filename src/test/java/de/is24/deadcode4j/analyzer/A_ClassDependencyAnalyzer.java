package de.is24.deadcode4j.analyzer;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

public final class A_ClassDependencyAnalyzer extends AnAnalyzer {

    private ClassDependencyAnalyzer objectUnderTest;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new ClassDependencyAnalyzer();
    }

    @Test
    public void reportsExistenceOfClassAndReportsItsDependencies() {
        objectUnderTest.doAnalysis(codeContext, getFile("SingleClass.class"));

        assertThat("Should analyze one class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), contains("SingleClass"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should analyze one class", codeDependencies.size(), is(1));
        assertThat("Should find dependency to java.lang.Object (only)", getOnlyElement(codeDependencies.values()), contains("java.lang.Object"));
    }

    @Test
    public void reportsTheDependencyOfAClassToAnother() {
        objectUnderTest.doAnalysis(codeContext, getFile("DependingClass.class"));

        assertThat("Should analyze one class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), contains("DependingClass"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should analyze one class", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), hasItem("IndependentClass"));
    }

    @Test
    public void recognizesDependenciesToInnerClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/classdependency/ClassWithInnerClasses.class"));

        assertThat("Should analyze one class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses"));

        Iterable<String> allDependencies = concat(codeContext.getAnalyzedCode().getCodeDependencies().values());
        assumeThat("Inner classes are only defined, but not used by the parent class!", allDependencies, containsInAnyOrder("java.lang.Object", "de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses$UsedStaticInnerClass"));
    }

    @Test
    public void recognizesDependencyOfInnerClassToParentClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/classdependency/ClassWithInnerClasses$UnusedInnerClass.class"));

        assertThat("Should analyze one class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses$UnusedInnerClass"));

        Iterable<String> allDependencies = concat(codeContext.getAnalyzedCode().getCodeDependencies().values());
        assertThat(allDependencies, containsInAnyOrder("java.lang.Object",
                "de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses"));
    }

    @Test
    public void recognizesNoDependencyOfStaticInnerClassToParentClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/classdependency/ClassWithInnerClasses$UnusedStaticInnerClass.class"));

        assertThat("Should analyze one class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("de.is24.deadcode4j.analyzer.classdependency.ClassWithInnerClasses$UnusedStaticInnerClass"));

        Iterable<String> allDependencies = concat(codeContext.getAnalyzedCode().getCodeDependencies().values());
        assumeThat("Although technically correct (to keep up the namespace, the parent class is required), the static inner class does not access the parent class!", allDependencies, containsInAnyOrder("java.lang.Object"));
    }

}
