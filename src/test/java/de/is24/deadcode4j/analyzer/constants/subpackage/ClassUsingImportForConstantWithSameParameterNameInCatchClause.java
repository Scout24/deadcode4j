package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.Constants;

@SuppressWarnings({"UnusedDeclaration", "ResultOfMethodCallIgnored"})
public class ClassUsingImportForConstantWithSameParameterNameInCatchClause {
    public String foo() {
        try {
            return null;
        } catch (ClassWithAccessibleField Constants) {
            return Constants.something;
        }
    }
    private static class ClassWithAccessibleField extends RuntimeException {
        public final String something = "something";
    }
    private static class InnerClass {
        public final String foo = Constants.FOO;
    }
}
