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

    private CodeContext codeContext;

    @Before
    public void setUp() throws Exception {
        codeContext = new CodeContext();
    }

    @Test
    public void recognizesDependencyToConstantInMethod() {
        Analyzer objectUnderTest = new ReferenceToConstantsAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInMethod.java"));
    }

    @Test
    public void recognizesDependencyToConstantInField() {
        Analyzer objectUnderTest = new ReferenceToConstantsAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingConstantInField.java"));
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantInField() {
        Analyzer objectUnderTest = new ReferenceToConstantsAnalyzer();

        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInField.java"));

        assertDependencyExists("de.is24.deadcode4j.analyzer.constants.ClassUsingFQConstantInField", "de.is24.deadcode4j.analyzer.constants.Constants");
    }

    private void assertDependencyExists(String depender, String dependee) {
        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat(codeDependencies.keySet(), containsInAnyOrder(depender));

        Iterable<String> allReportedClasses = concat(codeDependencies.values());
        assertThat(allReportedClasses, containsInAnyOrder(dependee));
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageInMethod() {
        Analyzer objectUnderTest = new ReferenceToConstantsAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingConstantOfOtherPackageInMethod.java"));
    }

    @Test
    public void recognizesDependencyToFullyQualifiedConstantOfOtherPackageInMethod() {
        Analyzer objectUnderTest = new ReferenceToConstantsAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingFQConstantOfOtherPackageInMethod.java"));
    }

    @Test
    public void recognizesDependencyToConstantOfOtherPackageReferencedViaStaticImportInMethod() {
        Analyzer objectUnderTest = new ReferenceToConstantsAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/subpackage/ClassUsingStaticImportForConstantOfOtherPackageInMethod.java"));
    }

    @Test
    public void recognizesOverwrittenConstantOfOtherPackageReferencedViaStaticImportInMethod() {
        Analyzer objectUnderTest = new ReferenceToConstantsAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingStaticImportForConstantWithSameLocalName.java"));
    }

    @Test
    public void recognizesDependencyToConstantReferencedViaStaticImportInExpression() {
        Analyzer objectUnderTest = new ReferenceToConstantsAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInExpression.java"));
    }

    @Test
    public void recognizesDependencyToInnerClassInsteadOfPackageClass() {
        Analyzer objectUnderTest = new ReferenceToConstantsAnalyzer();

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassWithInnerClassNamedLikePotentialTarget.java"));
    }

}
