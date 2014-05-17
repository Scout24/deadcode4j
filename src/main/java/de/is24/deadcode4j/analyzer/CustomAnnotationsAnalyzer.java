package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;

import javax.annotation.Nonnull;
import java.util.HashSet;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Analyzes class files: marks a class as being in use if it is annotated with one of the specified annotations.
 *
 * @since 1.3
 */
public final class CustomAnnotationsAnalyzer extends AnnotationsAnalyzer {

    @Nonnull
    private final HashSet<String> annotationsNotFoundInClassPath;

    /**
     * Creates a new <code>CustomAnnotationsAnalyzer</code>.
     *
     * @param customAnnotations a list of fully qualified (annotation) class names indicating a class is still in use
     * @since 1.3
     */
    public CustomAnnotationsAnalyzer(@Nonnull Iterable<String> customAnnotations) {
        super("_custom-annotations_", customAnnotations);
        annotationsNotFoundInClassPath = newHashSet(customAnnotations);
    }

    @Override
    public void finishAnalysis(@Nonnull CodeContext codeContext) {
        super.finishAnalysis(codeContext);
        annotationsNotFoundInClassPath.removeAll(getAnnotationsFoundInClassPath(codeContext));
    }

    @Override
    public void finishAnalysis() {
        super.finishAnalysis();
        for (String interfaceName : annotationsNotFoundInClassPath) {
            logger.warn("Annotation [{}] wasn't ever found in the class path. You should remove the configuration entry.", interfaceName);
        }
    }

}
