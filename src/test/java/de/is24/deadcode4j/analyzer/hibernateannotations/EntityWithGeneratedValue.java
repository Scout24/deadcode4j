package de.is24.deadcode4j.analyzer.hibernateannotations;

import javax.persistence.GeneratedValue;

@SuppressWarnings("UnusedDeclaration")
public class EntityWithGeneratedValue {
    @GeneratedValue(generator = "generatorOne")
    private String id;
}
