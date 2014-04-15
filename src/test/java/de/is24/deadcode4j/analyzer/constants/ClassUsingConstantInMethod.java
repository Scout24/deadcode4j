package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantInMethod {
    @Override
    public String toString() {
        return Constants.FOO;
    }
}
