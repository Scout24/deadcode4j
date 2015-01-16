package de.is24.deadcode4j.java8;

import java.util.HashSet;
import java.util.stream.Stream;

public class Lambda {
    public static void main(String[] args) {
        System.out.println(Stream.generate(HashSet<String>::new));
    }
}