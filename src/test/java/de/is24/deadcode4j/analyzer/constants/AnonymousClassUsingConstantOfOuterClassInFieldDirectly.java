package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class AnonymousClassUsingConstantOfOuterClassInFieldDirectly {
    public static final String FOO = "foo";
    private final Object o = new Object() {
        private final String bar = FOO;
    };
}
