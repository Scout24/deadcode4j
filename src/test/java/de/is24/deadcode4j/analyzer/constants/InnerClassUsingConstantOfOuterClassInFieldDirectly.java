package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class InnerClassUsingConstantOfOuterClassInFieldDirectly {
    public static final String FOO = "foo";
    private static class InnerClass {
        private final String bar = FOO;
    }
}
