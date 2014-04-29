package de.is24.deadcode4j.analyzer;

/**
 * Analyzes class files: marks a class as being in use if it is annotated with one of those Spring annotations:
 * <ul>
 * <li>org.springframework.jmx.export.annotation.ManagedResource</li>
 * <li>org.springframework.stereotype.Component</li>
 * </ul>
 * <p/>
 * Those Spring annotations are marked with <code>@Component</code> and thus are recursively considered as well:
 * <ul>
 * <li>org.springframework.context.annotation.Configuration</li>
 * <li>org.springframework.stereotype.Controller</li>
 * <li>org.springframework.stereotype.Service</li>
 * <li>org.springframework.stereotype.Repository</li>
 * </ul>
 *
 * @since 1.3
 */
public final class SpringAnnotationsAnalyzer extends AnnotationsAnalyzer {

    public SpringAnnotationsAnalyzer() {
        super("_Spring-Annotation_",
                "org.springframework.jmx.export.annotation.ManagedResource",
                "org.springframework.stereotype.Component");
    }

}
