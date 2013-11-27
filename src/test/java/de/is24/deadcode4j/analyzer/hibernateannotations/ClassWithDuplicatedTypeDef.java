package de.is24.deadcode4j.analyzer.hibernateannotations;

import org.hibernate.annotations.TypeDef;

@SuppressWarnings("unused")
@TypeDef(name = "aRandomType", typeClass = String.class)
public class ClassWithDuplicatedTypeDef {
}
