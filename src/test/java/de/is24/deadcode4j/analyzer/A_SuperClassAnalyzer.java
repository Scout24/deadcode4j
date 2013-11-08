package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public final class A_SuperClassAnalyzer extends AnAnalyzer {

    @Test
    public void reportsASubClassAsLiveCode() {
        Analyzer objectUnderTest = new SuperClassAnalyzer("junit", "de.is24.deadcode4j.analyzer.superclass.SuperClassMarkingCodeAsLive") {
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/superclass/SubClassOfSuperClassMarkingCodeAsLive.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertThat("Should have reported a dependency!", codeDependencies.size(), is(1));
        assertThat(concat(codeDependencies.values()), contains("de.is24.deadcode4j.analyzer.superclass.SubClassOfSuperClassMarkingCodeAsLive"));
    }

    @Test
    public void doesNotReportASubClassWithIrrelevantSuperClass() {
        Analyzer objectUnderTest = new SuperClassAnalyzer("junit", "de.is24.deadcode4j.analyzer.superclass.SuperClassMarkingCodeAsLive") {
        };

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/superclass/SubClassOfSomething.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertTrue("Should NOT have reported a dependency!", codeDependencies.isEmpty());
    }

}
