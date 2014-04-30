package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.Constants;
@SuppressWarnings({"UnusedDeclaration", "ResultOfMethodCallIgnored"})
public class ClassUsingImportForConstantWithSameParameterNameInConstructor {
    public ClassUsingImportForConstantWithSameParameterNameInConstructor(ClassWithAccessibleField Constants) {
        String temp = Constants.something;
        temp.intern();
    }
    private static class ClassWithAccessibleField {
        public final String something = "something";
    }
    private static class InnerClass {
        public final String foo = Constants.FOO;
    }
}
