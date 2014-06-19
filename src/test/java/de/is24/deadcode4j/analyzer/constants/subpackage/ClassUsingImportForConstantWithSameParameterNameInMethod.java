package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.Constants;

@SuppressWarnings("UnusedDeclaration")
public class ClassUsingImportForConstantWithSameParameterNameInMethod {
    public String foo(ClassWithAccessibleField Constants) {
        return Constants.something;
    }
    private static class ClassWithAccessibleField {
        public final String something = "something";
    }
    private static class InnerClass {
        public final String foo = Constants.FOO;
    }
}
