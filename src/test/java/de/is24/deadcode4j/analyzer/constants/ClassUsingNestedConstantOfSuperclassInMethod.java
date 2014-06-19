package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingNestedConstantOfSuperclassInMethod extends Superclass {
    @Override
    public String toString() {
        return More.STUFF;
    }
}
