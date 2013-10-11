package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_ClassFileAnalyzer extends AnAnalyzer {

    @Test
    public void parsesAClassFileReportsItsExistenceAndReportsItsDependencies() {
        ClassFileAnalyzer objectUnderTest = new ClassFileAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("SingleClass.class"));

        assertThat("Should analyze one class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should analyze one class", codeDependencies.size(), is(1));
        assertThat("Should find dependency to java.lang.Object (only)", getOnlyElement(codeDependencies.values()), contains("java.lang.Object"));
    }

    @Test
    public void reportsTheDependencyOfAClassToAnother() {
        ClassFileAnalyzer objectUnderTest = new ClassFileAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("DependingClass.class"));

        assertThat("Should analyze one class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should analyze one class", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), hasItem("IndependentClass"));
    }

    @Test
    public void doesNotParseNonClassFile() {
        ClassFileAnalyzer objectUnderTest = new ClassFileAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("spring.xml"));

        assertThat("Should analyze no class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(0));
        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should analyze no class", codeDependencies.size(), is(0));
    }

}
