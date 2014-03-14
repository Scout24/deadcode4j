package de.is24.deadcode4j.analyzer;

import javax.annotation.Nonnull;

/**
 * Analyzes class files: marks a class as being in use if it is annotated with one of the specified annotations.
 *
 * @since 1.3
 */
public final class CustomAnnotationsAnalyzer extends AnnotationsAnalyzer {

    /**
     * Creates a new <code>CustomAnnotationsAnalyzer</code>.
     *
     * @param customAnnotations a list of fully qualified (annotation) class names indicating a class is still in use
     * @since 1.3
     */
    public CustomAnnotationsAnalyzer(@Nonnull Iterable<String> customAnnotations) {
        super("_custom-annotations_", customAnnotations);
    }

}
