package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class AnonymousClassUsingConstantOfOuterClassInFieldDirectly {
    private static final String FOO = "foo";
    private static final Object OBJECT = new Object() {
        static final String BAR = FOO;
        Object anotherObject = new Object() {
            String barRef = BAR;
            Object crazyRef = OBJECT;
            String innerFooRef = Inner.INNER_FOO;
        };
    };
    private class Inner {
        private static final String INNER_FOO = "inner-foo";
        String bar = FOO;
    }
}
