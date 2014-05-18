package de.is24.deadcode4j;

import de.is24.deadcode4j.analyzer.AnalyzerAdapter;
import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.ModuleBuilder.givenModule;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_DeadCodeFinder {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();

    private DeadCodeFinder objectUnderTest;
    private Map<String, Set<String>> codeDependencies = newHashMap();

    @Before
    public void setUpObjectUnderTest() {
        Set<Analyzer> analyzers = emptySet();
        this.objectUnderTest = new DeadCodeFinder(analyzers);
        codeDependencies.clear();
    }

    @Test
    public void recognizesASingleClassAsDeadCode() {
        setUpDependency("SingleClass");
        Collection<String> deadCode = objectUnderTest.determineDeadClasses(provideAnalyzedCode());

        assertThat("Should recognize one class as dead", deadCode, hasSize(1));
        assertThat(deadCode, contains("SingleClass"));
    }

    @Test
    public void recognizesTwoInterdependentClassesAsLiveCode() {
        setUpDependency("A", "B");
        setUpDependency("B", "A");
        Collection<String> deadCode = objectUnderTest.determineDeadClasses(provideAnalyzedCode());

        assertThat("Should find NO dead code", deadCode, hasSize(0));
    }

    @Test
    public void recognizesDependencyChainAsPartlyDeadCode() {
        setUpDependency("DependingClass", "IndependentClass");
        setUpDependency("IndependentClass");
        Collection<String> deadCode = objectUnderTest.determineDeadClasses(provideAnalyzedCode());

        assertThat("Should recognize one class as dead", deadCode, hasSize(1));
    }

    @Test
    public void callsFinishAnalysisForEachModule() {
        final List<Module> reportedModules = newArrayList();
        objectUnderTest = new DeadCodeFinder(newHashSet(new AnalyzerAdapter() {
            @Override
            public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull File fileName) {
            }

            @Override
            public void finishAnalysis(@Nonnull CodeContext codeContext) {
                reportedModules.add(codeContext.getModule());
            }
        }));
        Module a = givenModule("A");
        Module b = givenModule("B");

        objectUnderTest.findDeadCode(newArrayList(a, b));

        assertThat(reportedModules, hasItems(a, b));
    }

    @Test
    public void callsFinishAnalysisForProject() {
        final AtomicBoolean finishAnalysisWasCalled = new AtomicBoolean(false);
        objectUnderTest = new DeadCodeFinder(newHashSet(new AnalyzerAdapter() {
            @Override
            public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull File fileName) {
            }

            @Override
            public void finishAnalysis() {
                finishAnalysisWasCalled.set(true);
            }
        }));

        objectUnderTest.findDeadCode(newArrayList(givenModule("A"), givenModule("B")));

        assertThat(finishAnalysisWasCalled.get(), is(true));
    }

    private void setUpDependency(String depender, String... dependees) {
        codeDependencies.put(depender, newHashSet(dependees));
    }

    private AnalyzedCode provideAnalyzedCode() {
        return new AnalyzedCode(EnumSet.noneOf(AnalysisStage.class), codeDependencies.keySet(), codeDependencies);
    }

}
