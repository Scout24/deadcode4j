package de.is24.deadcode4j.analyzer.constants;
import static de.is24.deadcode4j.analyzer.constants.Constants.FOO;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingStaticImportForConstantWithSameLocalName {
    public final String foo = FOO;

    @Override
    public String toString() {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String FOO = "test";
        return FOO;
    }
}
