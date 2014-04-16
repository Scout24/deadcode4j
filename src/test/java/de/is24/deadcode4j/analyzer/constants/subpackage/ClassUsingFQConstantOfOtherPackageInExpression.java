package de.is24.deadcode4j.analyzer.constants.subpackage;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingFQConstantOfOtherPackageInExpression {
    @Override
    public String toString() {
        return String.valueOf( "har".equals(de.is24.deadcode4j.analyzer.constants.Constants.FOO));
    }
}
