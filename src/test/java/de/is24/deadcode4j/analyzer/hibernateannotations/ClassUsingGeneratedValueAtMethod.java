package de.is24.deadcode4j.analyzer.hibernateannotations;

import javax.persistence.GeneratedValue;

@SuppressWarnings("UnusedDeclaration")
public class ClassUsingGeneratedValueAtMethod {
    @GeneratedValue(generator = "aGenerator")
    private String getId() {
        return null;
    }
}
