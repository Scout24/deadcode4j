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
    public void recognizesDependencyToConstantInMethod() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInMethod.java"));
    }

    @Test
    public void recognizesDependencyToConstantInField() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInField.java"));
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInField() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInField.java"));

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInField", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInMethod() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInMethod.java"));
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInMethod() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInMethod.java"));

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.subpackage.ClassUsingFQConstantOfOtherPackageInMethod", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageReferencedViaStaticImportInMethod() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingStaticImportForConstantOfOtherPackageInMethod.java"));
    }

    @Test
    public void recognizesOverwrittenConstantOfOtherPackageReferencedViaStaticImportInMethod() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameLocalName.java"));
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInExpression() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInExpression.java"));

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInExpression", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    @Test
    public void recognizesDependencyToInnerClassInsteadOfPackageClass() {
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassWithInnerClassNamedLikePotentialTarget.java"));
    }

    private void assertDependencyExists(String depender, String dependee) {
        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat(codeDependencies.keySet(), containsInAnyOrder(depender));

        Iterable<String> allReportedClasses = concat(codeDependencies.values());
        assertThat(allReportedClasses, containsInAnyOrder(dependee));
    }

}
