package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public final class A_TypeErasureAnalyzer extends AnAnalyzer<TypeErasureAnalyzer> {

    @Override
    protected TypeErasureAnalyzer createAnalyzer() {
        return new TypeErasureAnalyzer();
    }

    @Test
    public void recognizesClassTypeParameter() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/typeerasure/TypedArrayList.java");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList",
                "java.lang.Comparable",
                "java.math.BigDecimal",
                "java.util.ResourceBundle$Control",
                "java.util.Map$Entry",
                "java.util.regex.Pattern",
                "de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$InnerClass");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$InnerClass",
                "de.is24.deadcode4j.analyzer.typeerasure.PackageClass");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$SecondInnerClass",
                "de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$InnerClass$NestedInnerClass");
    }

    @Test
    public void recognizesDefaultPackageReference() {
        analyzeFile("../../src/test/java/ClassWithTypeArgument.java");

        assertThatDependenciesAreReportedFor("ClassWithTypeArgument",
                "TypeParameterClass");
    }

    @Test
    public void recognizesLowerBoundOfWildCard() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/typeerasure/ClassWithLowerBoundedWildCard.java");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.ClassWithLowerBoundedWildCard",
                "java.util.Collection");
    }

    @Test
    public void recognizesUpperBoundOfWildCard() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/typeerasure/ClassWithUpperBoundedWildCard.java");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.ClassWithUpperBoundedWildCard",
                "java.util.Collection");
    }

    @Test
    public void recognizesInheritedType() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/typeerasure/ClassWithInheritedType.java");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.ClassWithInheritedType",
                "de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$InnerClass");
    }

    @Test
    public void recognizesAnonymousClasses() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/typeerasure/ClassWithAnonymousClasses.java");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.ClassWithAnonymousClasses$1",
                "java.lang.String");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.ClassWithAnonymousClasses$2",
                "java.util.Set");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.ClassWithAnonymousClasses$1$1",
                "java.lang.Integer");
        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.ClassWithAnonymousClasses$1$2",
                "de.is24.deadcode4j.junit.SomeInterface");
    }

    @Test
    public void foo() {
        //analyzeFile("../../../../is24/legacy/importer/application/src/main/java/de/is24/imexp/ngimporter/mementoComparator/SimpleMementoComparator.java");
        analyzeFile("../../../../is24/legacy/importer/application/src/main/java/de/is24/imexp/ngimporter/ActionHandler.java");

        assertThatDependenciesAreReportedFor("de.is24.deadcode4j.analyzer.typeerasure.ClassWithInheritedType",
                "de.is24.deadcode4j.analyzer.typeerasure.TypedArrayList$InnerClass");
    }

}
