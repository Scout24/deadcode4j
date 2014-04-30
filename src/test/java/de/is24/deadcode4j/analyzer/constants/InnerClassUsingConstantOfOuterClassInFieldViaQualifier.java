package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class InnerClassUsingConstantOfOuterClassInFieldViaQualifier {
    public static final String FOO = "foo";
    private static class InnerClass {
        private final String bar = InnerClassUsingConstantOfOuterClassInFieldViaQualifier.FOO;
    }
}
