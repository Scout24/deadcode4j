package de.is24.deadcode4j.analyzer.classdependency;

@SuppressWarnings("UnusedDeclaration")
public class ClassWithInnerClasses {

    public ClassWithInnerClasses() {
        new UsedStaticInnerClass();
    }
    public static class UnusedStaticInnerClass {}
    public static class UsedStaticInnerClass {}
    public class UnusedInnerClass {}
}
