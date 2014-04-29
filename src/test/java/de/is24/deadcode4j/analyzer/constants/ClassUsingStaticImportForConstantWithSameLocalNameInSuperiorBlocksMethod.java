package de.is24.deadcode4j.analyzer.constants;
import java.util.Random;

import static de.is24.deadcode4j.analyzer.constants.Constants.FOO;

@SuppressWarnings("UnusedDeclaration")
public class ClassUsingStaticImportForConstantWithSameLocalNameInSuperiorBlocksMethod {
    @Override
    public String toString() {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String FOO = "test";
        do {
            if (new Random().nextBoolean()) {
                int i = 4 + 3;
                return FOO;
            }
        } while (true);
    }
    private static class InnerClass {
        public final String foo = FOO;
    }
}
