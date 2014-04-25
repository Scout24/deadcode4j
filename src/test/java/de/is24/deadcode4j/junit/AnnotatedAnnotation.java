package de.is24.deadcode4j.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Annotation
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface AnnotatedAnnotation {
}
