package de.is24.deadcode4j.analyzer.hibernateannotations;

import org.hibernate.annotations.Type;

@SuppressWarnings("UnusedDeclaration")
public class ClassUsingTypeWithoutTypeDef {
    @Type(type = "IndependentClass")
    private Number id;
}
