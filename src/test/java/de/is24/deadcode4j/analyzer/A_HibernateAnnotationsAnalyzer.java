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

public final class A_HibernateAnnotationsAnalyzer extends AFinalAnalyzer<HibernateAnnotationsAnalyzer> {

    private static <T> Matcher<T[]> hasVarArgItem(Matcher<? super T> elementMatcher) {
        return new MyVarArgsMatcher<T>(elementMatcher);
    }

    @Override
    protected HibernateAnnotationsAnalyzer createAnalyzer() {
        return new HibernateAnnotationsAnalyzer();
    }

    @Test
    public void reportsExistenceOfClasses() {
        objectUnderTest.doAnalysis(codeContext, getFile("A.class"));
        assertThatClassesAreReported("A");

        objectUnderTest.doAnalysis(codeContext, getFile("B.class"));
        assertThatClassesAreReported("A", "B");
    }

    @Test
    public void recognizesDependencyFromClassWithTypeAnnotatedFieldToTypeDefAnnotatedClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingTypeAtField.class"));
        finishAnalysis();

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingTypeAtField",
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassWithTypeDef");
    }

    @Test
    public void recognizesDependencyFromClassWithTypeAnnotatedMethodToTypeDefAnnotatedClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingTypeAtMethod.class"));
        finishAnalysis();

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingTypeAtMethod",
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassWithTypeDef");
    }

    @Test
    public void recognizesDependencyFromTypeAnnotatedClassesToTypeDefsAnnotatedPackage() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/Entity.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/AnotherEntity.class"));
        finishAnalysis();

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.Entity",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.AnotherEntity",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
    }

    @Test
    public void recognizesDependencyFromClassWithTypeAnnotatedMethodToReferencedClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingTypeWithoutTypeDef.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("IndependentClass.class"));
        finishAnalysis();

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingTypeWithoutTypeDef",
                "IndependentClass");
    }

    @Test
    public void reportsDependencyToDefinedStrategyIfStrategyIsPartOfTheAnalysis() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/knownStrategies/ClassDefiningGenericGenerator.class"));
        finishAnalysis();

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.knownStrategies.ClassDefiningGenericGenerator",
                "IndependentClass");
    }

    @Test
    public void doesNotReportDependencyToDefinedStrategyIfStrategyIsNoPartOfTheAnalysis() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class"));
        finishAnalysis();

        assertThatNoDependenciesAreReported();
    }

    @Test
    public void reportsDependencyFromPackageToDefinedStrategies() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/knownStrategies/package-info.class"));
        finishAnalysis();

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.knownStrategies.package-info",
                "IndependentClass", "DependingClass");
    }

    @Test
    public void recognizesDependencyFromClassWithGeneratedValueAnnotatedFieldToGenericGeneratorAnnotatedClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingGeneratedValueAtField.class"));
        finishAnalysis();


        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingGeneratedValueAtField",
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassDefiningGenericGenerator");
    }

    @Test
    public void recognizesDependencyFromClassWithGeneratedValueAnnotatedMethodToGenericGeneratorAnnotatedPackage() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassDefiningGenericGenerator.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassUsingGeneratedValueAtMethod.class"));
        finishAnalysis();


        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.ClassUsingGeneratedValueAtMethod",
                "de.is24.deadcode4j.analyzer.hibernateannotations.ClassDefiningGenericGenerator");
    }

    @Test
    public void recognizesDependencyFromGeneratedValueAnnotatedClassesToGenericGeneratorAnnotatedPackage() {
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/AnotherEntityWithGeneratedValue.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/EntityWithGeneratedValue.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class"));
        finishAnalysis();


        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.AnotherEntityWithGeneratedValue",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.EntityWithGeneratedValue",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
    }

    @Test
    public void issuesWarningForDuplicatedTypeDef() throws IllegalAccessException {
        Logger loggerMock = mock(Logger.class);
        ReflectionUtils.setVariableValueInObject(objectUnderTest, "logger", loggerMock);

        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithDuplicatedTypeDef.class"));
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
