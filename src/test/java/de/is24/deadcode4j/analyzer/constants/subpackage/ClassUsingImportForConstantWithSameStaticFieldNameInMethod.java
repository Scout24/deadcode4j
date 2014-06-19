package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.Constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingImportForConstantWithSameStaticFieldNameInMethod {
    private static final ClassWithAccessibleField Constants = new ClassWithAccessibleField();
    @Override
    public String toString() {
        return Constants.something;
    }
    private static class ClassWithAccessibleField {
        public final String something = "something";
    }
}
