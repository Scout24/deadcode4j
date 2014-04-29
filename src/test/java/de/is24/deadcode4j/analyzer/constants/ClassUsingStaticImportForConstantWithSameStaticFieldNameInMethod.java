package de.is24.deadcode4j.analyzer.constants;
import static de.is24.deadcode4j.analyzer.constants.Constants.FOO;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingStaticImportForConstantWithSameStaticFieldNameInMethod {
    private static final String FOO = "test";
    @Override
    public String toString() {
        return FOO;
    }
}
