package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_MainClassAnalyzer extends AByteCodeAnalyzer<MainClassAnalyzer> {

    @Override
    protected MainClassAnalyzer createAnalyzer() {
        return new MainClassAnalyzer();
    }

    @Test
    public void reportsMainClassAsBeingUsed() {
        analyzeFile("MainClass.class");

        assertThatDependenciesAreReported("MainClass");
    }

    @Test
    public void doesNotReportNonMainClassAsBeingUsed() {
        analyzeFile("SingleClass.class");

        assertThatNoDependenciesAreReported();
    }

}
