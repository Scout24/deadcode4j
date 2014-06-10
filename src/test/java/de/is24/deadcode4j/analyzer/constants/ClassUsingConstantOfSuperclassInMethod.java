package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantOfSuperclassInMethod extends Superclass {
    @Override
    public String toString() {
        return FOO;
    }
}
