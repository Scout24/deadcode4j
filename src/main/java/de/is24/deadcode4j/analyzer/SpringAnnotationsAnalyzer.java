package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;

import java.util.Arrays;

/**
 * Analyzes class files: marks a class as being in use if it is annotated with one of those Spring annotations:
 * <ul>
 * <li>org.springframework.jmx.export.annotation.ManagedResource</li>
 * <li>org.springframework.stereotype.Component</li>
 * <li>org.springframework.stereotype.Controller</li>
 * <li>org.springframework.stereotype.Service</li>
 * <li>org.springframework.stereotype.Repository</li>
 * </ul>
 *
 * @since 1.3.0
 */
public final class SpringAnnotationsAnalyzer extends AnnotationsAnalyzer implements Analyzer {

    public SpringAnnotationsAnalyzer() {
        super("_Spring-Annotation_", Arrays.asList(
                "org.springframework.jmx.export.annotation.ManagedResource",
                "org.springframework.stereotype.Component",
                "org.springframework.stereotype.Controller",
                "org.springframework.stereotype.Service",
                "org.springframework.stereotype.Repository"));
    }

}
