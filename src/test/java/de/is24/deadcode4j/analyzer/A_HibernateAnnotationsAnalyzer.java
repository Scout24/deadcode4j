package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public final class A_HibernateAnnotationsAnalyzer extends AnAnalyzer {

    private Analyzer objectUnderTest;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new HibernateAnnotationsAnalyzer();
    }

    @Test
    public void shouldRecognizeDependencyFromTypeClassToTypeDefAnnotatedClass() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/SomeClass.class"));


        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class file!", codeDependencies.size(), is(1));
        assertThat(codeDependencies.keySet(), contains("java.lang.String"));
        assertThat(concat(codeDependencies.values()), contains("de.is24.deadcode4j.analyzer.hibernateannotations.SomeClass"));
    }

}
