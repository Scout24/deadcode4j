package de.is24.deadcode4j.java8;

import java.util.HashSet;
import java.util.function.Supplier;

public class Lambda {
    private static <T> T get(Supplier<T> supplier) {
        return supplier.get();
    }
    public static void main(String[] args) {
        System.out.println(get(HashSet<String>::new));
    }
}