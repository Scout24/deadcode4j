package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.util.Collection;

public class DeadCode {
    private final Collection<String> analyzedClasses;
    private final Collection<String> deadClasses;

    public DeadCode(@Nonnull Collection<String> analyzedClasses, @Nonnull Collection<String> deadClasses) {
        this.analyzedClasses = analyzedClasses;
        this.deadClasses = deadClasses;
    }

    @Nonnull
    public Collection<String> getAnalyzedClasses() {
        return this.analyzedClasses;
    }

    @Nonnull
    public Collection<String> getDeadClasses() {
        return this.deadClasses;
    }

}
