package de.is24.deadcode4j;

import de.is24.deadcode4j.analyzer.AnalyzerAdapter;
import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.ModuleBuilder.givenModule;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_DeadCodeFinder {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();

    private DeadCodeFinder objectUnderTest;

    @Before
    public void setUpObjectUnderTest() {
        Set<Analyzer> analyzers = emptySet();
        this.objectUnderTest = new DeadCodeFinder(analyzers);
    }

    @Test
    public void callsFinishAnalysisForEachModule() {
        final List<Module> reportedModules = newArrayList();
        objectUnderTest = new DeadCodeFinder(newHashSet(new AnalyzerAdapter() {
            @Override
            public void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File fileName) {
            }

            @Override
            public void finishAnalysis(@Nonnull AnalysisContext analysisContext) {
                reportedModules.add(analysisContext.getModule());
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
            public void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File fileName) {
            }

            @Override
            public void finishAnalysis() {
                finishAnalysisWasCalled.set(true);
            }
        }));

        objectUnderTest.findDeadCode(newArrayList(givenModule("A"), givenModule("B")));

        assertThat(finishAnalysisWasCalled.get(), is(true));
    }

    @Test
    public void computesDeadCode() {
        objectUnderTest = new DeadCodeFinder(newHashSet(new AnalyzerAdapter() {
            @Override
            public void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File fileName) {
                analysisContext.addAnalyzedClass(fileName.getName());
            }
        }));

        DeadCode deadCode = objectUnderTest.findDeadCode(newArrayList(givenModule("A", new File("."))));

        assertThat(deadCode, is(notNullValue()));
        assertThat("Working directory should contain several files!", deadCode.getAnalyzedClasses(), hasSize(greaterThan(0)));
        assertThat("As no valid analyzer is set up, everything should be dead!", deadCode.getDeadClasses(), hasSize(greaterThan(0)));
    }

}
