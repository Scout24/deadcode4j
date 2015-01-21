package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_ReferenceToConstantsAnalyzer extends AnAnalyzer<ReferenceToConstantsAnalyzer> {
    private static final String FQ_CONSTANTS = "de.is24.deadcode4j.analyzer.constants.Constants";
    private Set<String> dependers = newHashSet();
    private List<String> dependees = newArrayList();

    @Before
    public void setUp() throws Exception {
        analysisContext.addAnalyzedClass(FQ_CONSTANTS); // make this class known to the context
        analysisContext.addAnalyzedClass(FQ_CONSTANTS + ".More");

        dependers.clear();
        dependees.clear();
    }

    @After
    public void assertNoOtherDependenciesExist() {
        Map<String, ? extends Iterable<String>> codeDependencies = analysisContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Classes with dependencies", codeDependencies.keySet(), equalTo(this.dependers));

        List<String> allReportedClasses = newArrayList(concat(codeDependencies.values()));
        assertThat("Classes being referenced", allReportedClasses, containsInAnyOrder(this.dependees.toArray()));
    }

    @Override
    protected ReferenceToConstantsAnalyzer createAnalyzer() {
        return new ReferenceToConstantsAnalyzer();
    }

    @Test
    public void recognizesDependencyToConstantInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInExpression.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantInExpression");
    }

    @Test
    public void recognizesDependencyToConstantInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantInField");
    }

    @Test
    public void recognizesDependencyToConstantInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantInMethod");
    }

    @Test
    public void recognizesDependencyToConstantOfImplementedInterfaceUsedByInnerClassInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/InnerClassUsingConstantOfImplementedInterfaceInExpression.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.InnerClassUsingConstantOfImplementedInterfaceInExpression$InnerClass");
    }

    @Test
    public void recognizesDependencyToConstantOfImplementedInterfaceInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantOfImplementedInterfaceInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantOfImplementedInterfaceInField");
    }

    @Test
    public void recognizesDependencyToConstantOfSuperclassInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantOfSuperclassInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantOfSuperclassInMethod");
    }

    @Test
    public void recognizesDependencyToNestedConstantOfImplementedInterfaceInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingNestedConstantOfImplementedInterfaceInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingNestedConstantOfImplementedInterfaceInField",
                FQ_CONSTANTS + "$More");
    }

    @Test
    public void recognizesDependencyToNestedConstantOfImplementedInterfaceUsedByInnerClassInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/InnerClassUsingNestedConstantOfImplementedInterfaceInExpression.java");
        triggerFinishAnalysisEvent();


        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.InnerClassUsingNestedConstantOfImplementedInterfaceInExpression$InnerClass",
                FQ_CONSTANTS + "$More");
    }

    @Test
    public void recognizesDependencyToNestedConstantOfSuperclassInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingNestedConstantOfSuperclassInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingNestedConstantOfSuperclassInMethod",
                FQ_CONSTANTS + "$More");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInExpression.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInExpression");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInField");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInMethod");
    }

    @Test
    public void recognizesDependencyToConstantViaStaticImportInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaStaticImportInExpression.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantViaStaticImportInExpression");
    }

    @Test
    public void recognizesDependencyToConstantViaStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaStaticImportInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantViaStaticImportInField");
    }

    @Test
    public void recognizesDependencyToConstantViaStaticImportInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaStaticImportInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantViaStaticImportInMethod");
    }

    @Test
    public void recognizesDependencyToConstantViaStaticImportInSwitch() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaStaticImportInSwitch.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantViaStaticImportInSwitch");
    }

    @Test
    public void recognizesDependencyToConstantViaAsteriskStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaAsteriskStaticImportInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantViaAsteriskStaticImportInField");
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageViaStaticImportIsOverwrittenByLocalVariable() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameLocalNameInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingStaticImportForConstantWithSameLocalNameInMethod$InnerClass");
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageViaStaticImportIsOverwrittenByLocalVariableInSuperiorBlock() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameLocalNameInSuperiorBlocksMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingStaticImportForConstantWithSameLocalNameInSuperiorBlocksMethod$InnerClass");
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageViaStaticImportIsOverwrittenByLocalVariableInStaticInitializer() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameLocalNameInStaticInitializer.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingStaticImportForConstantWithSameLocalNameInStaticInitializer$InnerClass");
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageViaStaticImportExistsAlthoughInnerClassDefinesInstanceFieldWithSameName() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameFieldNameDefinedByInnerClassInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingStaticImportForConstantWithSameFieldNameDefinedByInnerClassInMethod");
        triggerFinishAnalysisEvent();
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageViaStaticImportIsOverwrittenByInstanceFieldBeingDeclaredAfterItIsReferenced() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameFieldNameBeingDeclaredAfterItIsReferencedInMethod.java");
        triggerFinishAnalysisEvent();

        assertNoOtherDependenciesExist();
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageViaStaticImportIsOverwrittenByStaticField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameStaticFieldNameInMethod.java");
        triggerFinishAnalysisEvent();

        assertNoOtherDependenciesExist();
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageIsOverwrittenByCatchClauseParameter() {
        // no one says you cannot name a variable like an imported class :(
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingImportForConstantWithSameParameterNameInCatchClause.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingImportForConstantWithSameParameterNameInCatchClause$InnerClass");
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageIsOverwrittenByConstructorParameter() {
        // no one says you cannot name a variable like an imported class :(
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingImportForConstantWithSameParameterNameInConstructor.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingImportForConstantWithSameParameterNameInConstructor$InnerClass");
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageIsOverwrittenByLocalVariable() {
        // no one says you cannot name a variable like an imported class :(
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingImportForConstantWithSameLocalNameInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingImportForConstantWithSameLocalNameInMethod$InnerClass");
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageIsOverwrittenByInstanceField() {
        // not allowed by JVM: prefers field all the time; however, the import may be defined
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingImportForConstantWithSameFieldNameInMethod.java");
        triggerFinishAnalysisEvent();

        assertNoOtherDependenciesExist();
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageIsOverwrittenByEnum() {
        // not allowed by JVM: prefers field all the time; however, the import may be defined
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/EnumUsingImportForConstantWithSameEnumName.java");
        triggerFinishAnalysisEvent();

        assertNoOtherDependenciesExist();
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageIsOverwrittenByMethodParameter() {
        // no one says you cannot name a variable like an imported class :(
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingImportForConstantWithSameParameterNameInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingImportForConstantWithSameParameterNameInMethod$InnerClass");
    }

    @Test
    public void recognizesReferenceToConstantOfOtherPackageIsOverwrittenByStaticVariable() {
        // not allowed by JVM: prefers field all the time; however, the import may be defined
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingImportForConstantWithSameStaticFieldNameInMethod.java");
        triggerFinishAnalysisEvent();

        assertNoOtherDependenciesExist();
    }

    @Test
    public void recognizesDependencyToInnerClassInsteadOfPackageClass() {
        // make sure JVM specs don't mess with our assumptions
        assertThat(new ClassWithInnerClassNamedLikePotentialTarget().foo, is("bar"));
        assertThat(new ClassWithInnerClassNamedLikePotentialTarget.InnerClass().foo, is("bar"));

        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassWithInnerClassNamedLikePotentialTarget.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget",
                "de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget$Constants");
        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget$AnotherInnerClass");
        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget$InnerClass",
                "de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget$Constants");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInExpression.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageInExpression");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageInField");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageInMethod");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInExpression.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingFQConstantOfOtherPackageInExpression");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingFQConstantOfOtherPackageInField");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingFQConstantOfOtherPackageInMethod");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaAsteriskImportInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaAsteriskImportInExpression.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaAsteriskImportInExpression");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaAsteriskImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaAsteriskImportInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaAsteriskImportInField");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaAsteriskImportInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaAsteriskImportInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaAsteriskImportInMethod");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaStaticImportInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaStaticImportInExpression.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaStaticImportInExpression");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaStaticImportInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaStaticImportInField");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaStaticImportInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaStaticImportInMethod.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaStaticImportInMethod");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaAsteriskStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaAsteriskStaticImportInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaAsteriskStaticImportInField");
    }

    @Test
    public void recognizesEnumsDependencyToConstantInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/EnumUsingConstantInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.EnumUsingConstantInField");
    }

    @Test
    public void recognizesDependencyToConstantForInnerClassesInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingInnerClassOfConstantInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingInnerClassOfConstantInField",
                FQ_CONSTANTS + "$More");
    }

    @Test
    public void recognizesDependencyToConstantForInnerClassViaStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingInnerClassOfConstantViaStaticImportInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingInnerClassOfConstantViaStaticImportInField",
                FQ_CONSTANTS + "$More");
    }

    @Test
    public void recognizesDependencyToConstantOfInnerClassViaStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantOfInnerClassViaStaticImportInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantOfInnerClassViaStaticImportInField",
                FQ_CONSTANTS + "$More");
    }

    @Test
    public void recognizesDependencyToConstantForInnerClassViaAsteriskStaticImportInField() {
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageForInnerClassesInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingInnerClassOfConstantOfOtherPackageInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingInnerClassOfConstantOfOtherPackageInField",
                FQ_CONSTANTS + "$More");
    }

    @Test
    public void recognizesAnnotationsDependencyToConstantAsDefault() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/AnnotationUsingConstantAsDefault.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.AnnotationUsingConstantAsDefault");
        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.AnnotationUsingConstantAsDefault",
                "java.lang.annotation.ElementType");
    }

    @Test
    public void recognizesReferenceOfInnerClassToOuterClassInFieldViaQualifier() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/InnerClassUsingConstantOfOuterClassInFieldViaQualifier.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.InnerClassUsingConstantOfOuterClassInFieldViaQualifier$InnerClass",
                "de.is24.deadcode4j.analyzer.constants.InnerClassUsingConstantOfOuterClassInFieldViaQualifier");
    }

    @Test
    public void recognizesReferenceOfInnerClassToOuterClassInFieldDirectly() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/InnerClassUsingConstantOfOuterClassInFieldDirectly.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.InnerClassUsingConstantOfOuterClassInFieldDirectly$InnerClass",
                "de.is24.deadcode4j.analyzer.constants.InnerClassUsingConstantOfOuterClassInFieldDirectly");
    }

    @Test
    public void recognizesReferenceOfAnonymousClassToOuterClassInFieldDirectly() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/AnonymousClassUsingConstantOfOuterClassInFieldDirectly.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly$1",
                "de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly");
        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly$1$1",
                "de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly");
        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly$1$1",
                "de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly$1");
        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly$1$1",
                "de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly$Inner");
        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly$Inner",
                "de.is24.deadcode4j.analyzer.constants.AnonymousClassUsingConstantOfOuterClassInFieldDirectly");
    }

    @Ignore("Although this is no inlined constant it screws performance a bit, as we have no way of identifying the reference and thus perform many unnecessary class resolvings.")
    @Test
    public void recognizesReferenceToEnumerationInSwitch() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingEnumConstantInSwitch.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingEnumConstantInSwitch",
                "de.is24.deadcode4j.analyzer.constants.EnumUsingConstantInField");
    }

    @Test
    public void ignoresReferencesToStaticMethods() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticMethodInStaticField.java");
        triggerFinishAnalysisEvent();

        assertNoOtherDependenciesExist();
    }

    @Test
    public void ignoresReferencesToStaticMethodsOfToStaticallyImportedClasses() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticMethodOfStaticallyImportedClassInMethod.java");
        triggerFinishAnalysisEvent();
    }

    @Test
    public void ignoresReferencesToStaticMethodsOfNestedClasses() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticMethodOfNestedClassInMethod.java");
        triggerFinishAnalysisEvent();

        assertNoOtherDependenciesExist();
    }

    @Test
    public void recognizesReferenceToConstantBeingScopeOfAMethodCall() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassCallingMethodOfStaticallyImportedConstantInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassCallingMethodOfStaticallyImportedConstantInField");
    }

    @Test
    public void recognizesReferenceToConstantBeingPartialScopeOfAMethodCall() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassCallingMethodOnConstantOfImportedClassInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassCallingMethodOnConstantOfImportedClassInField");
    }

    @Test
    public void recognizesReferenceToNestedConstantBeingPartialScopeOfAMethodCall() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassCallingMethodOnConstantOfNestedClassOfImportedClassInField.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassCallingMethodOnConstantOfNestedClassOfImportedClassInField",
                FQ_CONSTANTS + "$More");
    }

    @Test
    public void doesNotRecognizeReferenceForInheritedNonConstantField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ReferenceToInheritedNonConstant.java");
    }

    @Test
    public void recognizesReferenceForInheritedConstantField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ReferenceToInheritedConstant.java");
        triggerFinishAnalysisEvent();

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ReferenceToInheritedConstant",
                "de.is24.deadcode4j.analyzer.constants.Superclass");
    }

    private void triggerFinishAnalysisEvent() {
        objectUnderTest.finishAnalysis(analysisContext);
    }

    private void assertDependencyExists(String depender, String dependee) {
        Map<String, ? extends Iterable<String>> codeDependencies = analysisContext.getAnalyzedCode().getCodeDependencies();
        assertThat(codeDependencies.keySet(), hasItem(depender));

        Iterable<String> allReportedClasses = concat(codeDependencies.values());
        assertThat(allReportedClasses, hasItem(dependee));
        this.dependers.add(depender);
        this.dependees.add(dependee);
    }

    private void assertDependencyToConstantsExists(String depender) {
        assertDependencyExists(depender, FQ_CONSTANTS);
    }

}
