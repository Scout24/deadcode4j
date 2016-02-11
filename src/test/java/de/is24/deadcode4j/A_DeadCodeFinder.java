package de.is24.deadcode4j;

import de.is24.deadcode4j.analyzer.AnalyzerAdapter;
import de.is24.deadcode4j.junit.FileLoader;
import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.ModuleBuilder.givenModule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_DeadCodeFinder {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();

    private DeadCodeFinder objectUnderTest;

    @Before
    public void setUpObjectUnderTest() {
        createObjectUnderTest();
    }

    @Test
    public void callsFinishAnalysisForEachModule() {
        final List<Module> reportedModules = newArrayList();
        createObjectUnderTest(new AnalyzerAdapter() {
            @Override
            public void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File fileName) {
            }

            @Override
            public void finishAnalysis(@Nonnull AnalysisContext analysisContext) {
                reportedModules.add(analysisContext.getModule());
            }
        });
        Module a = givenModule("A");
        Module b = givenModule("B");

        objectUnderTest.findDeadCode(newArrayList(a, b));

        assertThat(reportedModules, hasItems(a, b));
    }

    @Test
    public void callsFinishAnalysisForProject() {
        createObjectUnderTest(new AnalyzerAdapter() {
            @Override
            public void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File fileName) {
            }

            @Override
            public void finishAnalysis(@Nonnull AnalysisSink analysisSink, @Nonnull AnalyzedCode analyzedCode) {
                analysisSink.addAnalyzedClass("A");
                analysisSink.addException(AnalysisStage.DEADCODE_ANALYSIS);
            }
        });

        DeadCode deadCode = objectUnderTest.findDeadCode(newArrayList(givenModule("A"), givenModule("B")));

        assertThat(deadCode.getAnalyzedClasses(), contains("A"));
        assertThat(deadCode.getStagesWithExceptions(), contains(AnalysisStage.DEADCODE_ANALYSIS));
    }

    @Test
    public void computesDeadCode() {
        createObjectUnderTest(new AnalyzerAdapter() {
            @Override
            public void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File fileName) {
                analysisContext.addAnalyzedClass(fileName.getName());
            }
        });

        DeadCode deadCode = objectUnderTest.findDeadCode(newArrayList(givenModule("A", FileLoader.getFile("."))));

        assertThat(deadCode, is(notNullValue()));
        assertThat("Working directory should contain several files!", deadCode.getAnalyzedClasses(), hasSize(greaterThan(0)));
        assertThat("As no valid analyzer is set up, everything should be dead!", deadCode.getDeadClasses(), hasSize(greaterThan(0)));
    }

    private void createObjectUnderTest(Analyzer... analyzers) {
        this.objectUnderTest = new DeadCodeFinder(new DeadCodeComputer(), newHashSet(analyzers));
    }

}
