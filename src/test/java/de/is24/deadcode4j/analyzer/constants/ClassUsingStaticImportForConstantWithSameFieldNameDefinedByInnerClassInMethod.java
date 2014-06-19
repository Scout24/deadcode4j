package de.is24.deadcode4j.analyzer.constants;
import static de.is24.deadcode4j.analyzer.constants.Constants.FOO;

@SuppressWarnings("UnusedDeclaration")
public class ClassUsingStaticImportForConstantWithSameFieldNameDefinedByInnerClassInMethod {
    private int bar = 42;
    @Override
    public String toString() {
        return FOO;
    }
    private class InnerClass {
        private final String FOO = "test";

        @Override
        public String toString() {
            return "" + bar;
        }
    }
}
