package de.is24.deadcode4j.analyzer.hibernateannotations;

import javax.persistence.GeneratedValue;

@SuppressWarnings("UnusedDeclaration")
public class AnotherEntityWithGeneratedValue {
    @GeneratedValue(generator = "generatorThree")
    private String getId() {
        return null;
    }
}
