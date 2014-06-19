package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingFQConstantInMethod {
    @Override
    public String toString() {
        return de.is24.deadcode4j.analyzer.constants.Constants.FOO;
    }
}
