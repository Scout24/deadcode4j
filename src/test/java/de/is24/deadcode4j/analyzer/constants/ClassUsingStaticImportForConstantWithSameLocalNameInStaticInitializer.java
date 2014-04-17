package de.is24.deadcode4j.analyzer.constants;
import static de.is24.deadcode4j.analyzer.constants.Constants.FOO;

@SuppressWarnings("UnusedDeclaration")
public class ClassUsingStaticImportForConstantWithSameLocalNameInStaticInitializer {
    static {
        do {
            String FOO = "test";
            FOO.notify();
        } while (false);
    }

    private static class InnerClass {
        public final String foo = FOO;
    }
}
