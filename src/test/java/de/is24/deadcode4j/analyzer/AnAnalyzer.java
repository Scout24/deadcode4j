package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.*;
import de.is24.deadcode4j.junit.FileLoader;
import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class AnAnalyzer<T extends Analyzer> {

    protected T objectUnderTest;
    protected AnalysisContext analysisContext;
    protected boolean analysisIsFinished;

    @Rule
    public LoggingRule enableLogging() {
        return new LoggingRule();
    }

    @Before
    public final void initAnalyzer() {
        objectUnderTest = createAnalyzer();
    }

    @Before
    public final void initAnalysisContext() {
        analysisContext = AnalysisContextBuilder.givenAnalysisContext(
                ModuleBuilder.givenModule("de.is24:deadcode4j-junit"));
        analysisIsFinished = false;
    }

    @After
    public void doFinishAnalysis() {
        finishAnalysisIfNecessary();
    }

    protected T createAnalyzer() {
        return null;
    }

    protected void analyzeFile(String fileName) {
        System.out.println("Testing " + objectUnderTest);
        objectUnderTest.doAnalysis(analysisContext, FileLoader.getFile(fileName));
    }

    protected void finishAnalysis() {
        this.objectUnderTest.finishAnalysis(this.analysisContext);
        this.objectUnderTest.finishAnalysis(this.analysisContext, this.analysisContext.getAnalyzedCode());
        this.analysisIsFinished = true;
    }

    protected void finishAnalysisIfNecessary() {
        if (!analysisIsFinished) {
            finishAnalysis();
        }
    }

    protected void assertThatClassesAreReported(String... classes) {
        assertThat(analysisContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder(classes));
    }

    protected void assertThatDependenciesAreReportedFor(String depender, String... dependee) {
        finishAnalysisIfNecessary();
        Map<String, Set<String>> codeDependencies = analysisContext.getAnalyzedCode().getCodeDependencies();
        assertThat("No dependencies were reported for [" + depender + "]!",
                codeDependencies, hasEntry(equalTo(depender), any(Set.class)));
        assertThat("Incorrect dependencies reported for [" + depender + "]!",
                codeDependencies.get(depender), containsInAnyOrder(dependee));
    }

    protected void assertThatDependenciesAreReported(String... dependee) {
        finishAnalysisIfNecessary();
        Map<String, Set<String>> codeDependencies = analysisContext.getAnalyzedCode().getCodeDependencies();
        Iterable<String> allReportedDependees = concat(codeDependencies.values());
        assertThat(allReportedDependees, containsInAnyOrder(dependee));
    }

    protected void assertThatNoDependenciesAreReported() {
        finishAnalysisIfNecessary();
        Map<String, Set<String>> codeDependencies = analysisContext.getAnalyzedCode().getCodeDependencies();
        assertThat(codeDependencies.size(), is(0));
    }

    protected void assertThatIntermediateResultIsStored() {
        doFinishAnalysis();
        assertThat(this.analysisContext.getCache(), hasEntry(anything(), instanceOf(IntermediateResult.class)));
    }

}
