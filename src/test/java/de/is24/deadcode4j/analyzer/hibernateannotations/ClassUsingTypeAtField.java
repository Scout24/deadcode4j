package de.is24.deadcode4j.analyzer.hibernateannotations;

import org.hibernate.annotations.Type;

@SuppressWarnings("UnusedDeclaration")
public class ClassUsingTypeAtField {
    @Type(type = "aRandomType")
    private String id;
}
