package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.junit.Test;

public final class A_ReferenceToConstantsAnalyzer extends AnAnalyzer {

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

        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("../../src/test/java/de/is24/deadcode4j/analyzer/constants/ClassUsingFQConstantInField.java"));
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

}
