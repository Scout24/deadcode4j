package de.is24.junit;

import de.is24.junit.typedefs.CustomClass;
import de.is24.junit.typedefs.CustomClass;

import javax.persistence.Id;
import org.hibernate.annotations.Type;

public class EntityClass {

    @Id
    @Type(type = "customClass")
    private String id;

}