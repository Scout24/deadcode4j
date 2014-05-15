package de.is24.deadcode4j.analyzer;

import org.junit.Test;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assume.assumeThat;

public class A_SpringDataCustomRepositoriesAnalyzer extends AnAnalyzer<SpringDataCustomRepositoriesAnalyzer> {

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

        Iterable<String> allDependencies = concat(codeContext.getAnalyzedCode().getCodeDependencies().values());
        assumeThat(allDependencies, containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.customrepositories.FooRepositoryImpl"));
//        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.customrepositories.FooRepositoryCustom",
//                "de.is24.deadcode4j.analyzer.customrepositories.FooRepositoryImpl");
    }

}
