package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public final class A_ReferenceToConstantsAnalyzer extends AnAnalyzer {

    private Analyzer objectUnderTest;
    private CodeContext codeContext;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new ReferenceToConstantsAnalyzer();
        codeContext = new CodeContext();
    }

    @Test
    public void recognizesDependencyToConstantInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInExpression.java");
    }

    @Test
    public void recognizesDependencyToConstantInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInField.java");
    }

    @Test
    public void recognizesDependencyToConstantInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInMethod.java");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInExpression.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInExpression", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInField.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInField", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInMethod.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInMethod", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToConstantViaStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaStaticImportInField.java");
    }

    @Test
    public void recognizesDependencyToConstantViaAsteriskStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantViaAsteriskStaticImportInField.java");
    }

    @Test
    public void recognizesOverwrittenConstantOfOtherPackageReferencedViaStaticImportInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameLocalName.java");
    }

    @Test
    public void recognizesDependencyToInnerClassInsteadOfPackageClass() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassWithInnerClassNamedLikePotentialTarget.java");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInExpression.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageInExpression", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInField.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageInField", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInMethod.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingConstantOfOtherPackageInMethod", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInExpression() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInExpression.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingFQConstantOfOtherPackageInExpression", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInField.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingFQConstantOfOtherPackageInField", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInMethod() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInMethod.java");

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingFQConstantOfOtherPackageInMethod", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaAsteriskImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaAsteriskImportInField.java");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaStaticImportInField.java");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageViaAsteriskStaticImportInField() {
        analyzeFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageViaAsteriskStaticImportInField.java");
    }

    private void analyzeFile(String fileName) {
        objectUnderTest.doAnalysis(codeContext, getFile(fileName));
    }

    private void assertDependencyExists(String depender, String dependee) {
        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat(codeDependencies.keySet(), containsInAnyOrder(depender));

        Iterable<String> allReportedClasses = concat(codeDependencies.values());
        assertThat(allReportedClasses, containsInAnyOrder(dependee));
    }

}
