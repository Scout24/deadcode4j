package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.codehaus.plexus.util.ReflectionUtils;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsArrayContaining;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.matchers.VarargMatcher;
import org.slf4j.Logger;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

    @Test
    public void reportsDependencyToDefinedStrategyIfStrategyIsPartOfTheAnalysis() {
        CodeContext codeContext = new CodeContext();

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("IndependentClass.class"));
        objectUnderTest.finishAnalysis(codeContext);

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(2));
        assertThat(codeDependencies.keySet(), containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassDefiningGenericGenerator"));
        assertThat(concat(codeDependencies.values()), contains("IndependentClass"));
    }

    @Test
    public void doesNotReportDependencyToDefinedStrategyIfStrategyIsNoPartOfTheAnalysis() {
        CodeContext codeContext = new CodeContext();

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class"));
        objectUnderTest.finishAnalysis(codeContext);

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertTrue("Should not report any dependency!", codeDependencies.isEmpty());
    }

    @Test
    public void reportsDependencyFromPackageToDefinedStrategies() {
        CodeContext codeContext = new CodeContext();

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("IndependentClass.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("DependingClass.class"));
        objectUnderTest.finishAnalysis(codeContext);

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(3));
        assertThat(codeDependencies.keySet(), containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info"));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder("IndependentClass", "DependingClass"));
    }

    @Test
    public void recognizesDependencyFromClassWithGeneratedValueAnnotatedFieldToGenericGeneratorAnnotatedClass() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingGeneratedValueAtField.class"));
        objectUnderTest.finishAnalysis(codeContext);


        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(2));
        assertThat(codeDependencies.keySet(), containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingGeneratedValueAtField"));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassDefiningGenericGenerator"));
    }

    @Test
    public void recognizesDependencyFromClassWithGeneratedValueAnnotatedMethodToGenericGeneratorAnnotatedPackage() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingGeneratedValueAtMethod.class"));
        objectUnderTest.finishAnalysis(codeContext);


        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(2));
        assertThat(codeDependencies.keySet(), containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingGeneratedValueAtMethod"));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassDefiningGenericGenerator"));
    }

    @Test
    public void recognizesDependencyFromGeneratedValueAnnotatedClassesToGenericGeneratorAnnotatedPackage() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/AnotherEntityWithGeneratedValue.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/EntityWithGeneratedValue.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class"));
        objectUnderTest.finishAnalysis(codeContext);


        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the class files!", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(3));
        assertThat(codeDependencies.keySet(), containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.hibernateannotations.AnotherEntityWithGeneratedValue",
                "de.is24.deadcode4j.analyzer.hibernateannotations.EntityWithGeneratedValue"));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder(
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info"));
    }

    @Test
    public void issuesWarningForDuplicatedTypeDef() throws IllegalAccessException {
        Logger loggerMock = mock(Logger.class);
        ReflectionUtils.setVariableValueInObject(objectUnderTest, "logger", loggerMock);

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithDuplicatedTypeDef.class"));
        objectUnderTest.finishAnalysis(codeContext);

        verify(loggerMock).warn(
                Matchers.contains("@TypeDef"),
                argThat(hasVarArgItem(equalTo("aRandomType"))));
    }

    private static <T> Matcher<T[]> hasVarArgItem(Matcher<? super T> elementMatcher) {
        return new MyVarArgsMatcher<T>(elementMatcher);
    }

    private static class MyVarArgsMatcher<T> extends IsArrayContaining<T> implements VarargMatcher {
        public MyVarArgsMatcher(Matcher<? super T> elementMatcher) {
            super(elementMatcher);
        }
    }

}
