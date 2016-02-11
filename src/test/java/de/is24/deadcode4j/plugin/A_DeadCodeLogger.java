package de.is24.deadcode4j.plugin;

import de.is24.deadcode4j.AnalysisStage;
import de.is24.deadcode4j.DeadCode;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class A_DeadCodeLogger {

    private final Collection<String> noClasses = Collections.emptyList();
    private DeadCodeLogger objectUnderTest;
    private Log logMock;

    private static Collection<String> classes(String... classes) {
        return asList(classes);
    }

    @Before
    public void setUpMojo() {
        logMock = mock(Log.class);
        objectUnderTest = new DeadCodeLogger(logMock);
    }

    @Test
    public void logsThatNoDeadCodeWasFound() throws Exception {
        DeadCode deadCode = new DeadCode(noExceptions(), classes("A", "B"), noClasses);

        objectUnderTest.log(deadCode);

        verifyNumberOfAnalyzedClassesIs(2);
        verifyNoDeadCodeWasFound();
    }

    @Test
    public void logsThatOneDeadClassOfThreeWasFound() throws Exception {
        DeadCode deadCode = new DeadCode(noExceptions(), classes("A", "B", "SingleClass"), classes("SingleClass"));

        objectUnderTest.log(deadCode);

        verifyNumberOfAnalyzedClassesIs(3);
        verify(logMock).warn("Found 1 unused class(es):");
        verify(logMock).warn("  SingleClass");
    }

    @Test
    public void logsThatAnExceptionOccurredDuringFileAnalysis() {
        DeadCode deadCode = new DeadCode(exceptionAt(AnalysisStage.FILE_ANALYSIS), noClasses, noClasses);

        objectUnderTest.log(deadCode);

        verify(logMock, times(1)).warn(anyString());
    }

    @Test
    public void logsThatAnExceptionOccurredDuringGeneralSetup() {
        DeadCode deadCode = new DeadCode(exceptionAt(AnalysisStage.GENERAL_SETUP), noClasses, noClasses);

        objectUnderTest.log(deadCode);

        verify(logMock, times(1)).error(anyString());
    }

    @Test
    public void logsThatAnExceptionOccurredDuringModuleSetup() {
        DeadCode deadCode = new DeadCode(exceptionAt(AnalysisStage.MODULE_SETUP), noClasses, noClasses);

        objectUnderTest.log(deadCode);

        verify(logMock, times(1)).warn(anyString());
    }

    @Test
    public void logsThatAnExceptionOccurredDuringDeadCodeAnalysis() {
        DeadCode deadCode = new DeadCode(exceptionAt(AnalysisStage.DEADCODE_ANALYSIS), noClasses, noClasses);

        objectUnderTest.log(deadCode);

        verify(logMock, times(1)).warn(anyString());
    }

    private EnumSet<AnalysisStage> noExceptions() {
        return EnumSet.noneOf(AnalysisStage.class);
    }

    private EnumSet<AnalysisStage> exceptionAt(AnalysisStage fileAnalysis) {
        return EnumSet.of(fileAnalysis);
    }

    private void verifyNumberOfAnalyzedClassesIs(int count) {
        verify(logMock).info("Analyzed " + count + " class(es).");
    }

    private void verifyNoDeadCodeWasFound() {
        verify(logMock).info("No unused classes found. Rejoice!");
    }

}
