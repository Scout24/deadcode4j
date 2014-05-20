package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContextBuilder;
import de.is24.deadcode4j.IntermediateResult;
import org.codehaus.plexus.util.ReflectionUtils;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsArrayContaining;
import org.junit.Test;
import org.mockito.internal.matchers.VarargMatcher;
import org.slf4j.Logger;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.AnalysisContextBuilder.givenAnalysisContext;
import static de.is24.deadcode4j.IntermediateResultMapBuilder.givenIntermediateResultMap;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class A_HibernateAnnotationsAnalyzer extends AByteCodeAnalyzer<HibernateAnnotationsAnalyzer> {

    private static <T> Matcher<T[]> hasVarArgItem(Matcher<? super T> elementMatcher) {
        return new MyVarArgsMatcher<T>(elementMatcher);
    }

    @Override
    protected HibernateAnnotationsAnalyzer createAnalyzer() {
        return new HibernateAnnotationsAnalyzer();
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
    public void storesTypeDefinitionsAsIntermediateResults() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class");

        assertThatIntermediateResultIsStored();
    }

    @Test
    public void storesTypeUsagesAsIntermediateResults() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/Entity.class");

        assertThatIntermediateResultIsStored();
    }

    @Test
    public void considersTypeDefinitionsFromIntermediateResults() {
        this.analysisContext = givenAnalysisContext(
                this.analysisContext.getModule(),
                HibernateAnnotationsAnalyzer.class.getName() + "|typeDefinitions",
                givenIntermediateResultMap("byteClass", "Foo"));

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/Entity.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.Entity", "Foo");
    }

    @Test
    public void prefersOwnTypeDefinitionsOverIntermediateResults() {
        this.analysisContext = givenAnalysisContext(
                this.analysisContext.getModule(),
                HibernateAnnotationsAnalyzer.class.getName() + "|typeDefinitions",
                givenIntermediateResultMap("byteClass", "Foo"));

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/Entity.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.Entity",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
    }

    @Test
    public void considersTypeUsagesFromIntermediateResults() {
        this.analysisContext = givenAnalysisContext(
                this.analysisContext.getModule(),
                HibernateAnnotationsAnalyzer.class.getName() + "|typeUsages",
                givenIntermediateResultMap("byteClass", newHashSet("Foo", "Bar")));

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class");

        assertThatDependenciesAreReportedFor("Foo", "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
        assertThatDependenciesAreReportedFor("Bar", "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
    }

    @Test
    public void ignoresTypeUsagesFromIntermediateResultsForTypeDefinitionsFromIntermediateResults() {
        Map<Object, IntermediateResult> intermediateResults = newHashMap();
        intermediateResults.put(HibernateAnnotationsAnalyzer.class.getName() + "|typeUsages",
                givenIntermediateResultMap("byteClass", newHashSet("Bar")));
        intermediateResults.put(HibernateAnnotationsAnalyzer.class.getName() + "|typeDefinitions",
                givenIntermediateResultMap("byteClass", "Foo"));
        this.analysisContext = AnalysisContextBuilder.givenAnalysisContext(this.analysisContext.getModule(), intermediateResults);

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/EntityWithGeneratedValue.class");

        assertThatNoDependenciesAreReported();
    }

    @Test
    public void storesGeneratorDefinitionsAsIntermediateResults() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class");

        assertThatIntermediateResultIsStored();
    }

    @Test
    public void storesGeneratorUsagesAsIntermediateResults() {
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/EntityWithGeneratedValue.class");

        assertThatIntermediateResultIsStored();
    }

    @Test
    public void considersGeneratorDefinitionsFromIntermediateResults() {
        this.analysisContext = givenAnalysisContext(
                this.analysisContext.getModule(),
                HibernateAnnotationsAnalyzer.class.getName() + "|generatorDefinitions",
                givenIntermediateResultMap("generatorOne", "Foo"));

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/EntityWithGeneratedValue.class");

        assertThatDependenciesAreReportedFor(
                "de.is24.deadcode4j.analyzer.hibernateannotations.EntityWithGeneratedValue", "Foo");
    }

    @Test
    public void prefersOwnGeneratorDefinitionsOverIntermediateResults() {
        this.analysisContext = givenAnalysisContext(
                this.analysisContext.getModule(),
                HibernateAnnotationsAnalyzer.class.getName() + "|generatorDefinitions",
                givenIntermediateResultMap("generatorOne", "Foo"));

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/EntityWithGeneratedValue.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.hibernateannotations.EntityWithGeneratedValue",
                "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
    }

    @Test
    public void considersGeneratorUsagesFromIntermediateResults() {
        this.analysisContext = givenAnalysisContext(
                this.analysisContext.getModule(),
                HibernateAnnotationsAnalyzer.class.getName() + "|generatorUsages",
                givenIntermediateResultMap("generatorOne", newHashSet("Foo", "Bar")));

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/package-info.class");

        assertThatDependenciesAreReportedFor("Foo", "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
        assertThatDependenciesAreReportedFor("Bar", "de.is24.deadcode4j.analyzer.hibernateannotations.package-info");
    }

    @Test
    public void ignoresGeneratorUsagesFromIntermediateResultsForGeneratorDefinitionsFromIntermediateResults() {
        Map<Object, IntermediateResult> intermediateResults = newHashMap();
        intermediateResults.put(HibernateAnnotationsAnalyzer.class.getName() + "|generatorUsages",
                givenIntermediateResultMap("generatorOne", newHashSet("Bar")));
        intermediateResults.put(HibernateAnnotationsAnalyzer.class.getName() + "|generatorDefinitions",
                givenIntermediateResultMap("generatorOne", "Foo"));
        this.analysisContext = AnalysisContextBuilder.givenAnalysisContext(this.analysisContext.getModule(), intermediateResults);

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/Entity.class");

        assertThatNoDependenciesAreReported();
    }

    @Test
    public void issuesWarningForDuplicatedTypeDef() throws IllegalAccessException {
        Logger loggerMock = mock(Logger.class);
        ReflectionUtils.setVariableValueInObject(objectUnderTest, "logger", loggerMock);

        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithTypeDef.class");
        analyzeFile("de/is24/deadcode4j/analyzer/hibernateannotations/ClassWithDuplicatedTypeDef.class");
        finishAnalysis();

        verify(loggerMock).warn(
                org.mockito.Matchers.contains("@TypeDef"),
                (Object[]) argThat(hasVarArgItem(equalTo("aRandomType"))));
    }

    private static class MyVarArgsMatcher<T> extends IsArrayContaining<T> implements VarargMatcher {
        public MyVarArgsMatcher(Matcher<? super T> elementMatcher) {
            super(elementMatcher);
        }
    }

}
