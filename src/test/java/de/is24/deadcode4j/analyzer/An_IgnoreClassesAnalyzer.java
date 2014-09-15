package de.is24.deadcode4j.analyzer;

import com.google.common.collect.Sets;
import de.is24.deadcode4j.DeadCodeComputer;
import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import static org.mockito.Mockito.verify;

public class An_IgnoreClassesAnalyzer extends AByteCodeAnalyzer<IgnoreClassesAnalyzer> {

    private Log logMock;

    @Override
    public LoggingRule enableLogging() {
        logMock = LoggingRule.createMock();
        return new LoggingRule(logMock);
    }

    @Override
    protected IgnoreClassesAnalyzer createAnalyzer() {
        return new IgnoreClassesAnalyzer(new DeadCodeComputer(), Sets.newHashSet("A", "C"));
    }

    @Test
    public void marksIgnoredClassAsLiveCode() throws Exception {
        analyzeFile("A.class");
        doFinishAnalysis();

        assertThatDependenciesAreReported("A");
    }

    @Test
    public void doesNotMarkUnknownClassAsLiveCode() throws Exception {
        analyzeFile("B.class");
        doFinishAnalysis();

        assertThatNoDependenciesAreReported();
    }

    @Test
    public void logsThatAClassWasIgnored() throws Exception {
        analyzeFile("A.class");
        doFinishAnalysis();

        verify(logMock).info("Ignoring 1 class(es) which seem(s) to be unused.");
    }

    @Test
    public void logsThatAnIgnoredClassDoesNotExist() throws Exception {
        analyzeFile("A.class");
        doFinishAnalysis();

        verify(logMock).warn("Class [C] should be ignored, but does not exist. You should remove the configuration entry.");
    }

    @Test
    public void logsThatAnIgnoredClassIsRequiredAnyway() throws Exception {
        analysisContext.addDependencies("B", "A");

        analyzeFile("A.class");
        doFinishAnalysis();

        verify(logMock).warn("Class [A] should be ignored, but is not dead. You should remove the configuration entry.");
    }

}