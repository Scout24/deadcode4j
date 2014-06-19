package de.is24.deadcode4j.analyzer.constants;

public interface Constants {
    String FOO = "foo";
    int BAR = 42;

    public interface More {
        String STUFF = "acme";
    }

    public class Inner {
        public static void call() {}
    }
}
