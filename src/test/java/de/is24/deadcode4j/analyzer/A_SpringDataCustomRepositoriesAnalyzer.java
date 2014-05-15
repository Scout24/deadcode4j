package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.IntermediateResult;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.CodeContextBuilder.givenCodeContext;
import static de.is24.deadcode4j.IntermediateResults.resultSetFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

    @Test
    public void storesCustomRepositoryInterfacesAsIntermediateResults() {
        analyzeFile("de/is24/deadcode4j/analyzer/customrepositories/FooRepository.class");

        doFinishAnalysis();
        assertThat(this.codeContext.getCache(), hasEntry(anything(), instanceOf(IntermediateResult.class)));
    }

    @Test
    public void reportsImplementationsOfIntermediateResults() {
        this.codeContext = givenCodeContext(
                this.codeContext.getModule(),
                this.objectUnderTest.getClass(),
                resultSetFor(newHashSet("de.is24.deadcode4j.analyzer.customrepositories.FooRepositoryCustom")));

        analyzeFile("de/is24/deadcode4j/analyzer/customrepositories/FooRepositoryImpl.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.customrepositories.FooRepository",
                "de.is24.deadcode4j.analyzer.customrepositories.FooRepositoryImpl");
    }

}
