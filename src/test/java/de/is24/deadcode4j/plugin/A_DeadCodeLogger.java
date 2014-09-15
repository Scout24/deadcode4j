package de.is24.deadcode4j.plugin;

import com.google.common.collect.Sets;
import de.is24.deadcode4j.AnalysisStage;
import de.is24.deadcode4j.AnalyzedCode;
import de.is24.deadcode4j.DeadCode;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class A_DeadCodeLogger {

    private DeadCodeLogger objectUnderTest;
    private Log logMock;

    private static Set<String> noClasses() {
        return Collections.emptySet();
    }

    private static Set<String> classes(String... classes) {
        return Sets.newHashSet(classes);
    }

    private static EnumSet<AnalysisStage> noExceptions() {
        return EnumSet.noneOf(AnalysisStage.class);
    }

    private static EnumSet<AnalysisStage> exceptionAt(AnalysisStage fileAnalysis) {
        return EnumSet.of(fileAnalysis);
    }

    private static AnalyzedCode analyzedCodeWith(EnumSet<AnalysisStage> stagesWithExceptions, Set<String> analyzedClasses) {
        return new AnalyzedCode(stagesWithExceptions, analyzedClasses, Collections.<String, Set<String>>emptyMap());
    }

    @Before
    public void setUpMojo() {
        logMock = mock(Log.class);
        objectUnderTest = new DeadCodeLogger(logMock);
    }

    @Test
    public void logsThatNoDeadCodeWasFound() throws Exception {
        DeadCode deadCode = new DeadCode(analyzedCodeWith(noExceptions(), classes("A", "B")), noClasses());

        objectUnderTest.log(deadCode);

        verifyNumberOfAnalyzedClassesIs(2);
        verifyNoDeadCodeWasFound();
    }

    @Test
    public void logsThatOneDeadClassOfThreeWasFound() throws Exception {
        DeadCode deadCode = new DeadCode(analyzedCodeWith(noExceptions(), classes("A", "B", "SingleClass")), classes("SingleClass"));

        objectUnderTest.log(deadCode);

        verifyNumberOfAnalyzedClassesIs(3);
        verify(logMock).warn("Found 1 unused class(es):");
        verify(logMock).warn("  SingleClass");
    }

    @Test
    public void logsThatAnExceptionOccurred() {
        DeadCode deadCode = new DeadCode(analyzedCodeWith(exceptionAt(AnalysisStage.FILE_ANALYSIS), noClasses()), noClasses());

        objectUnderTest.log(deadCode);

        verify(logMock).warn("At least one file could not be parsed; analysis may be inaccurate!");
    }

    private void verifyNumberOfAnalyzedClassesIs(int count) {
        verify(logMock).info("Analyzed " + count + " class(es).");
    }

    private void verifyNoDeadCodeWasFound() {
        verify(logMock).info("No unused classes found. Rejoice!");
    }

}
