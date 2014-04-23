package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * <code>AnalyzedCode</code> comprises the classes being analyzed as well as the code dependencies.
 *
 * @since 1.0.0
 */
public class AnalyzedCode {
    private final Set<String> analyzedClasses;
    private final Map<String, Set<String>> codeDependencies;

    public AnalyzedCode(@Nonnull Set<String> analyzedClasses, @Nonnull Map<String, Set<String>> codeDependencies) {
        this.analyzedClasses = analyzedClasses;
        this.codeDependencies = codeDependencies;
    }

    @Nonnull
    public Set<String> getAnalyzedClasses() {
        return analyzedClasses;
    }

    /**
     * Returns a map consisting of code artifacts (typically classes) pointing to their dependencies.
     */
    @Nonnull
    public Map<String, Set<String>> getCodeDependencies() {
        return codeDependencies;
    }

}
