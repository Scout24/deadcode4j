package de.is24.deadcode4j.analyzer.constants;
import static de.is24.deadcode4j.analyzer.constants.Constants.Inner;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingStaticMethodOfStaticallyImportedClassInMethod {
    public ClassUsingStaticMethodOfStaticallyImportedClassInMethod() {
        Inner.call();
    }
}
