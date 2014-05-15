package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public class A_SpringDataCustomRepositoriesAnalyzer extends AByteCodeAnalyzer<SpringDataCustomRepositoriesAnalyzer> {

    @Override
    protected SpringDataCustomRepositoriesAnalyzer createAnalyzer() {
        return new SpringDataCustomRepositoriesAnalyzer();
    }

    @Test
    public void recognizesCustomRepository() {
        analyzeFile("de/is24/deadcode4j/analyzer/customrepositories/FooRepository.class");
        analyzeFile("de/is24/deadcode4j/analyzer/customrepositories/FooRepositoryCustom.class");
        analyzeFile("de/is24/deadcode4j/analyzer/customrepositories/FooRepositoryImpl.class");
        doFinishAnalysis();

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.customrepositories.FooRepository",
                "de.is24.deadcode4j.analyzer.customrepositories.FooRepositoryImpl");
    }

}
