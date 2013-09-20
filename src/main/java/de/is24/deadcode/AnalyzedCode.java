package de.is24.deadcode;

import javax.annotation.Nonnull;
import java.util.Map;

public class AnalyzedCode {
    private final Iterable<String> analyzedClasses;
    private final Map<String, Iterable<String>> dependenciesForClass;

    public AnalyzedCode(@Nonnull Iterable<String> analyzedClasses, @Nonnull Map<String, Iterable<String>> dependenciesForClass) {
        this.analyzedClasses = analyzedClasses;
        this.dependenciesForClass = dependenciesForClass;
    }

    @Nonnull
    public Iterable<String> getAnalyzedClasses() {
        return analyzedClasses;
    }

    @Nonnull
    public Map<String, Iterable<String>> getDependenciesForClass() {
        return dependenciesForClass;
    }

}
