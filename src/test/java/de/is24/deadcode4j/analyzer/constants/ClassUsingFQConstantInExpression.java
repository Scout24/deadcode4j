package de.is24.deadcode4j.analyzer.constants;

import static de.is24.deadcode4j.analyzer.constants.Constants.FOO;

@SuppressWarnings("UnusedDeclaration")
public class ClassUsingFQConstantInExpression {
    @Override
    public String toString() {
        return String.valueOf( "har".equals(FOO));
    }
}
