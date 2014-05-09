package de.is24.deadcode4j.analyzer;

import org.codehaus.plexus.util.ReflectionUtils;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsArrayContaining;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.matchers.VarargMatcher;
import org.slf4j.Logger;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class A_HibernateAnnotationsAnalyzer extends AnAnalyzer<HibernateAnnotationsAnalyzer> {

    private static <T> Matcher<T[]> hasVarArgItem(Matcher<? super T> elementMatcher) {
        return new MyVarArgsMatcher<T>(elementMatcher);
    }

    @Override
    protected HibernateAnnotationsAnalyzer createAnalyzer() {
        return new HibernateAnnotationsAnalyzer();
    }

    @Test
    public void reportsExistenceOfClasses() {
        analyzeFile("A.class");
        assertThatClassesAreReported("A");

        analyzeFile("B.class");
        assertThatClassesAreReported("A", "B");
    }

    @Test
    public void recognizesDependencyFromClassWithTypeAnnotatedFieldToTypeDefAnnotatedClass() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingTypeAtField.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingTypeAtField",
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassWithTypeDef");
    }

    @Test
    public void recognizesDependencyFromClassWithTypeAnnotatedMethodToTypeDefAnnotatedClass() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingTypeAtMethod.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingTypeAtMethod",
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassWithTypeDef");
    }

    @Test
    public void recognizesDependencyFromTypeAnnotatedClassesToTypeDefsAnnotatedPackage() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/Entity.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/AnotherEntity.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.Entity",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.AnotherEntity",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
    }

    @Test
    public void recognizesDependencyFromClassWithTypeAnnotatedMethodToReferencedClass() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingTypeWithoutTypeDef.class");
        analyzeFile("IndependentClass.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingTypeWithoutTypeDef",
                "IndependentClass");
    }

    @Test
    public void reportsDependencyToDefinedStrategyIfStrategyIsPartOfTheAnalysis() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/knownStrategies/ClassDefiningGenericGenerator.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.knownStrategies.ClassDefiningGenericGenerator",
                "IndependentClass");
    }

    @Test
    public void doesNotReportDependencyToDefinedStrategyIfStrategyIsNoPartOfTheAnalysis() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class");

        assertThatNoDependenciesAreReported();
    }

    @Test
    public void reportsDependencyFromPackageToDefinedStrategies() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/knownStrategies/package-info.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.knownStrategies.package-info",
                "IndependentClass", "DependingClass");
    }

    @Test
    public void recognizesDependencyFromClassWithGeneratedValueAnnotatedFieldToGenericGeneratorAnnotatedClass() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingGeneratedValueAtField.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingGeneratedValueAtField",
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassDefiningGenericGenerator");
    }

    @Test
    public void recognizesDependencyFromClassWithGeneratedValueAnnotatedMethodToGenericGeneratorAnnotatedPackage() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingGeneratedValueAtMethod.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingGeneratedValueAtMethod",
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassDefiningGenericGenerator");
    }

    @Test
    public void recognizesDependencyFromGeneratedValueAnnotatedClassesToGenericGeneratorAnnotatedPackage() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/AnotherEntityWithGeneratedValue.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/EntityWithGeneratedValue.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.AnotherEntityWithGeneratedValue",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.EntityWithGeneratedValue",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
    }

    @Test
    public void issuesWarningForDuplicatedTypeDef() throws IllegalAccessException {
        Logger loggerMock = mock(Logger.class);
        ReflectionUtils.setVariableValueInObject(objectUnderTest, "logger", loggerMock);

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithDuplicatedTypeDef.class");
        finishAnalysis();

        verify(loggerMock).warn(
                Matchers.contains("@TypeDef"),
                (Object[]) argThat(hasVarArgItem(equalTo("aRandomType"))));
    }

    private static class MyVarArgsMatcher<T> extends IsArrayContaining<T> implements VarargMatcher {
        public MyVarArgsMatcher(Matcher<? super T> elementMatcher) {
            super(elementMatcher);
        }
    }

}
