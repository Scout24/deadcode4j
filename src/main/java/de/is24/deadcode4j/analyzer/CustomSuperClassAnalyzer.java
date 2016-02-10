package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.AnalysisSink;
import de.is24.deadcode4j.AnalyzedCode;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Analyzes class files: marks a class as being in use if it is a direct subclass of one of the specified classes.
 *
 * @since 1.4
 */
public final class CustomSuperClassAnalyzer extends SuperClassAnalyzer {

    @Nonnull
    private final Set<String> superClassesNotFoundInClassPath;

    /**
     * Creates a new <code>CustomAnnotationsAnalyzer</code>.
     *
     * @param customSuperClasses a list of fully qualified class names indicating that the extending class is in use
     * @since 1.4
     */
    public CustomSuperClassAnalyzer(@Nonnull Iterable<String> customSuperClasses) {
        super("_custom-superclass_", customSuperClasses);
        superClassesNotFoundInClassPath = newHashSet(customSuperClasses);
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisContext analysisContext) {
        super.finishAnalysis(analysisContext);
        superClassesNotFoundInClassPath.removeAll(getSuperClassesFoundInClassPath(analysisContext));
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisSink analysisSink, @Nonnull AnalyzedCode analyzedCode) {
        super.finishAnalysis(analysisSink, analyzedCode);
        for (String interfaceName : superClassesNotFoundInClassPath) {
            logger.warn("SuperClass [{}] wasn't ever found in the class path. You should remove the configuration entry.", interfaceName);
        }
    }

}
