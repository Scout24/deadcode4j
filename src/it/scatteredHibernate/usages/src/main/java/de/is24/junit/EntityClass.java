package de.is24.junit;

import javax.persistence.Id;
import org.hibernate.annotations.Type;

public class EntityClass {

    @Id
    @Type(type = "numberClass")
    private long id;

    @Type(type = "customClass")
    private String content;

}