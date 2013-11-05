package de.is24.deadcode4j.analyzer.hibernateannotations;

import org.hibernate.annotations.Type;

public class AnotherEntity {

    @Type(type = "shortClass")
    private Number id;

}
