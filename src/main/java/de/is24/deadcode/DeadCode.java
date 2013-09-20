package de.is24.deadcode;

import javax.annotation.Nonnull;

public class DeadCode {
    private final Iterable<String> analyzedClasses;
    private final Iterable<String> deadClasses;

    public DeadCode(@Nonnull Iterable<String> analyzedClasses, @Nonnull Iterable<String> deadClasses) {
        this.analyzedClasses = analyzedClasses;
        this.deadClasses = deadClasses;
    }

    @Nonnull
    public Iterable<String> getAnalyzedClasses() {
        return this.analyzedClasses;
    }

    @Nonnull
    public Iterable<String> getDeadClasses() {
        return this.deadClasses;
    }

}
