package de.is24.deadcode4j.analyzer.constants;

@SuppressWarnings("UnusedDeclaration")
public class ClassWithInnerClassNamedLikePotentialTarget {
    public final String foo = Constants.FOO;

    private class Constants {
        public static final String FOO = "foo";
    }
}
