package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.*;
import de.is24.deadcode4j.junit.FileLoader;
import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Before;
import org.junit.Rule;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class AnAnalyzer<T extends Analyzer> {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();
    protected T objectUnderTest;
    protected CodeContext codeContext;

    @Before
    public final void initAnalyzer() {
        objectUnderTest = createAnalyzer();
    }

    @Before
    public final void initCodeContext() {
        Module dummyModule = new Module(
                "de.is24:deadcode4j-junit",
                "UTF-8",
                Collections.<Resource>emptyList(),
                null,
                Collections.<Repository>emptyList());
        codeContext = new CodeContext(dummyModule);
    }

    protected T createAnalyzer() {
        return null;
    }

    protected void analyzeFile(String fileName) {
        objectUnderTest.doAnalysis(codeContext, FileLoader.getFile(fileName));
    }

    protected void finishAnalysis() {
        this.objectUnderTest.finishAnalysis(this.codeContext);
    }

    protected void assertThatClassesAreReported(String... classes) {
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder(classes));
    }

    protected void assertThatDependenciesAreReportedFor(String depender, String... dependee) {
        Map<String, Set<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat(codeDependencies, hasEntry(equalTo(depender), any(Set.class)));
        assertThat(codeDependencies.get(depender), containsInAnyOrder(dependee));
    }

    protected void assertThatDependenciesAreReported(String... dependee) {
        Map<String, Set<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        Iterable<String> allReportedDependees = concat(codeDependencies.values());
        assertThat(allReportedDependees, containsInAnyOrder(dependee));
    }

    protected void assertThatNoDependenciesAreReported() {
        Map<String, Set<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat(codeDependencies.size(), is(0));
    }

}
