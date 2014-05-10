package de.is24.junit;

import javax.persistence.Id;
import org.hibernate.annotations.Type;

public class EntityClass {

    @Id
    @Type(type = "customClass")
    private String id;

}