package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassWithInnerClassNamedLikePotentialTarget {
    public final String foo = Constants.FOO;
    private static interface Constants {
        public static final String FOO = "bar";
    }
    public static class InnerClass {
        public final String foo = Constants.FOO;
    }
    public static class AnotherInnerClass {
        public final String foo = de.is24.deadcode4j.analyzer.constants.Constants.FOO;
    }
}
