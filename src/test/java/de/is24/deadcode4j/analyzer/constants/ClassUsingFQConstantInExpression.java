package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingFQConstantInExpression {
    @Override
    public String toString() {
        return String.valueOf( "har".equals(de.is24.deadcode4j.analyzer.constants.Constants.FOO));
    }
}
