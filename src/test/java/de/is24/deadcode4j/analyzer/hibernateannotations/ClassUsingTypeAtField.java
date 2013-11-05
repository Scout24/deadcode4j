package de.is24.deadcode4j.analyzer.hibernateannotations;

import org.hibernate.annotations.Type;

import javax.persistence.Id;

public class ClassUsingTypeAtField {
    @Id
    @Type(type = "aRandomType")
    private String id;
}
