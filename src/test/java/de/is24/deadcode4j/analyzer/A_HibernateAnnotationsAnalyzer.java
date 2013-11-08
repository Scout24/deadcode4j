package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_HibernateAnnotationsAnalyzer extends AnAnalyzer {

    private Analyzer objectUnderTest;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new HibernateAnnotationsAnalyzer();
    }

    @Test
    public void recognizesDependencyFromClassWithTypeAnnotatedFieldToTypeDefAnnotatedClass() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingTypeAtField.class"));
        objectUnderTest.finishAnalysis(codeContext);


        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(2));
        assertThat(codeDependencies.keySet(), contains("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingTypeAtField"));
        assertThat(concat(codeDependencies.values()), contains("de.is24.deadcode4j.analyzer.hibernateannotations.ClassWithTypeDef"));
    }

    @Test
    public void recognizesDependencyFromClassWithTypeAnnotatedMethodToTypeDefAnnotatedClass() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingTypeAtMethod.class"));
        objectUnderTest.finishAnalysis(codeContext);


        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(2));
        assertThat(codeDependencies.keySet(), contains("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingTypeAtMethod"));
        assertThat(concat(codeDependencies.values()), contains("de.is24.deadcode4j.analyzer.hibernateannotations.ClassWithTypeDef"));
    }

    @Test
    public void recognizesDependencyFromTypeAnnotatedClassesToTypeDefsAnnotatedPackage() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/Entity.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/AnotherEntity.class"));
        objectUnderTest.finishAnalysis(codeContext);


        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(3));
        assertThat(codeDependencies.keySet(), containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.hibernateannotations.Entity",
                "de.is24.deadcode4j.analyzer.hibernateannotations.AnotherEntity"));
        assertThat(concat(codeDependencies.values()), contains(
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info"));
    }

    @Test
    public void recognizesDependencyFromClassWithTypeAnnotatedMethodToReferencedClass() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingTypeWithoutTypeDef.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("IndependentClass.class"));
        objectUnderTest.finishAnalysis(codeContext);


        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(2));
        assertThat(codeDependencies.keySet(), hasItem("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingTypeWithoutTypeDef"));
        assertThat(concat(codeDependencies.values()), contains("IndependentClass"));
    }

}
