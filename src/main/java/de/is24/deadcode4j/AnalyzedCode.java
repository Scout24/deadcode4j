package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * <code>AnalyzedCode</code> comprises the classes being analyzed as well as the code dependencies.
 *
 * @since 1.0.0
 */
public class AnalyzedCode {
    private final Collection<String> analyzedClasses;
    private final Map<String, ? extends Iterable<String>> codeDependencies;

    public AnalyzedCode(@Nonnull Collection<String> analyzedClasses, @Nonnull Map<String, ? extends Iterable<String>> codeDependencies) {
        this.analyzedClasses = analyzedClasses;
        this.codeDependencies = codeDependencies;
    }

    @Nonnull
    public Collection<String> getAnalyzedClasses() {
        return analyzedClasses;
    }

    @Nonnull
    public Map<String, ? extends Iterable<String>> getCodeDependencies() {
        return codeDependencies;
    }

}
