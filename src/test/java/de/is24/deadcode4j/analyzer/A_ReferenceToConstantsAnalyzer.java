package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_ReferenceToConstantsAnalyzer extends AnAnalyzer {
    private static final String FQ_CONSTANTS = "de.is24.deadcode4j.analyzer.constants.Constants";
    private Analyzer objectUnderTest;
    private CodeContext codeContext;
    private Set<String> dependers = newHashSet();
    private List<String> dependees = newArrayList();

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new ReferenceToConstantsAnalyzer();
        codeContext = new CodeContext();
        codeContext.addAnalyzedClass(FQ_CONSTANTS); // make this class known to the context

        dependers.clear();
        dependees.clear();
    }

    @After
    public void assertNoOtherDependenciesExist() throws Exception {
        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat(codeDependencies.keySet(), equalTo(this.dependers));

        List<String> allReportedClasses = newArrayList(concat(codeDependencies.values()));
        assertThat(allReportedClasses, containsInAnyOrder(this.dependees.toArray()));
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
    public void recognizesDependencyToFullyQualifiedConstantInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInExpression.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInExpression");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInField.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInField");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInMethod.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInMethod");
    }

    @Test
    public void recognizesDependencyToConstantViaStaticImportInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaStaticImportInExpression.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantViaStaticImportInExpression");
    }

    @Test
    public void recognizesDependencyToConstantViaStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaStaticImportInField.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantViaStaticImportInField");
    }

    @Test
    public void recognizesDependencyToConstantViaStaticImportInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaStaticImportInMethod.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingConstantViaStaticImportInMethod");
    }

    @Test
    public void recognizesDependencyToConstantViaAsteriskStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaAsteriskStaticImportInField.java");
    }

    @Test
    public void recognizesOverwrittenConstantOfOtherPackageReferencedViaStaticImportInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameLocalNameInMethod.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingStaticImportForConstantWithSameLocalNameInMethod$InnerClass");
    }

    @Test
    public void recognizesOverwrittenConstantOfOtherPackageReferencedViaStaticImportInStaticInitializer() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameLocalNameInStaticInitializer.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.ClassUsingStaticImportForConstantWithSameLocalNameInStaticInitializer$InnerClass");
    }

    @Test
    public void recognizesDependencyToInnerClassInsteadOfPackageClass() {
        // make sure JVM specs don't mess with our assumptions
        assertThat(new ClassWithInnerClassNamedLikePotentialTarget().foo, is("bar"));
        assertThat(new ClassWithInnerClassNamedLikePotentialTarget.InnerClass().foo, is("bar"));

        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassWithInnerClassNamedLikePotentialTarget.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget",
                "de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget$Constants");
        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget$AnotherInnerClass",
                FQ_CONSTANTS);
        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget$InnerClass",
                "de.is24.deadcode4j.analyzer.constants.ClassWithInnerClassNamedLikePotentialTarget$Constants");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInExpression.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageInExpression");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInField.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageInField");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInMethod.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageInMethod");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInExpression.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingFQConstantOfOtherPackageInExpression");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInField.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingFQConstantOfOtherPackageInField");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInMethod.java");

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

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaStaticImportInExpression");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaStaticImportInField.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaStaticImportInField");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaStaticImportInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaStaticImportInMethod.java");

        assertDependencyToConstantsExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageViaStaticImportInMethod");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaAsteriskStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaAsteriskStaticImportInField.java");
    }

    private void analyzeFile(String fileName) {
        objectUnderTest.doAnalysis(codeContext, getFile(fileName));
    }

    private void triggerFinishAnalysisEvent() {
        objectUnderTest.finishAnalysis(codeContext);
    }

    private void assertDependencyExists(String depender, String dependee) {
        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
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
