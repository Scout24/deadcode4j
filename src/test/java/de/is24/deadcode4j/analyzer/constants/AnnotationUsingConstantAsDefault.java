package de.is24.deadcode4j.analyzer.constants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
@SuppressWarnings("UnusedDeclaration")
@Target(ElementType.METHOD)
public @interface AnnotationUsingConstantAsDefault {
    String foo() default Constants.FOO;
}
