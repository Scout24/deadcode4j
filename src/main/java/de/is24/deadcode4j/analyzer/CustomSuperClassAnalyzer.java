package de.is24.deadcode4j.analyzer;

import javax.annotation.Nonnull;

/**
 * Analyzes class files: marks a class as being in use if it is a direct subclass of one of the specified classes.
 *
 * @since 1.4
 */
public final class CustomSuperClassAnalyzer extends SuperClassAnalyzer {

    /**
     * Creates a new <code>CustomAnnotationsAnalyzer</code>.
     *
     * @param customSuperClasses a list of fully qualified class names indicating that the extending class is in use
     * @since 1.4
     */
    public CustomSuperClassAnalyzer(@Nonnull Iterable<String> customSuperClasses) {
        super("_custom-superclass_", customSuperClasses);
    }

}
