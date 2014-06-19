package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantInExpression {
    @Override
    public String toString() {
        return String.valueOf( "har".equals(Constants.FOO));
    }
}
