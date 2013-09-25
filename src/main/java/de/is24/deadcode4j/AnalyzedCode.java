package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public class AnalyzedCode {
    private final Collection<String> analyzedClasses;
    private final Map<String, Iterable<String>> dependenciesForClass;

    public AnalyzedCode(@Nonnull Collection<String> analyzedClasses, @Nonnull Map<String, Iterable<String>> dependenciesForClass) {
        this.analyzedClasses = analyzedClasses;
        this.dependenciesForClass = dependenciesForClass;
    }

    @Nonnull
    public Collection<String> getAnalyzedClasses() {
        return analyzedClasses;
    }

    @Nonnull
    public Map<String, Iterable<String>> getDependenciesForClass() {
        return dependenciesForClass;
    }

}
