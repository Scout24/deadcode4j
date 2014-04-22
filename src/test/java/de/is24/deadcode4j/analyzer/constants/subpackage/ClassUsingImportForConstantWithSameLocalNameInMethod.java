package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.Constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingImportForConstantWithSameLocalNameInMethod {
    @Override
    public String toString() {
        @SuppressWarnings("UnnecessaryLocalVariable")
        ClassWithAccessibleField Constants = new ClassWithAccessibleField();
        return Constants.something;
    }
    private static class InnerClass {
        public final String foo = Constants.FOO;
    }
    private static class ClassWithAccessibleField {
        public final String something = "something";
    }
}
