package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingStaticMethodOfNestedClassInMethod {
    public ClassUsingStaticMethodOfNestedClassInMethod() {
        Constants.Inner.call();
    }
}
