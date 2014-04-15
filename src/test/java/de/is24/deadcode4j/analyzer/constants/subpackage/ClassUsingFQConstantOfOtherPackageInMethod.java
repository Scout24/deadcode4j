package de.is24.deadcode4j.analyzer.constants.subpackage;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingFQConstantOfOtherPackageInMethod {
    @Override
    public String toString() {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String localVariable = de.is24.deadcode4j.analyzer.constants.Constants.FOO;
        return localVariable;
    }
}
