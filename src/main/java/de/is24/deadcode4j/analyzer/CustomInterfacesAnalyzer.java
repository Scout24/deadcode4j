package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.AnalysisSink;
import de.is24.deadcode4j.AnalyzedCode;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Analyzes class files: marks a class as being in use if it explicitly implements one of the specified interfaces.
 *
 * @since 1.4
 */
public final class CustomInterfacesAnalyzer extends InterfacesAnalyzer {

    @Nonnull
    private final Set<String> interfacesNotFoundInClassPath;

    /**
     * Creates a new <code>CustomInterfacesAnalyzer</code>.
     *
     * @param customInterfaces a list of fully qualified interface names indicating that the implementing class is in use
     * @since 1.4
     */
    public CustomInterfacesAnalyzer(@Nonnull Iterable<String> customInterfaces) {
        super("_custom-interfaces_", customInterfaces);
        interfacesNotFoundInClassPath = newHashSet(customInterfaces);
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisContext analysisContext) {
        super.finishAnalysis(analysisContext);
        interfacesNotFoundInClassPath.removeAll(getInterfacesFoundInClassPath(analysisContext));
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisSink analysisSink, @Nonnull AnalyzedCode analyzedCode) {
        super.finishAnalysis(analysisSink, analyzedCode);
        for (String interfaceName : interfacesNotFoundInClassPath) {
            logger.warn("Interface [{}] wasn't ever found in the class path. You should remove the configuration entry.", interfaceName);
        }
    }

}
