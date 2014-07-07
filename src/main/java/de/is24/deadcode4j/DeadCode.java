package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;

/**
 * <code>DeadCode</code> provides the results of the {@link DeadCodeFinder}.
 *
 * @since 1.0.0
 */
public class DeadCode {
    @Nonnull
    private final AnalyzedCode analyzedCode;
    @Nonnull
    private final Collection<String> deadClasses;

    public DeadCode(@Nonnull AnalyzedCode analyzedCode, @Nonnull Collection<String> deadClasses) {
        this.analyzedCode = analyzedCode;
        this.deadClasses = deadClasses;
    }

    @Nonnull
    public Collection<String> getAnalyzedClasses() {
        return this.analyzedCode.getAnalyzedClasses();
    }

    @Nonnull
    public Collection<String> getDeadClasses() {
        return this.deadClasses;
    }

    /**
     * Returns the stages for which an exception occurred.
     *
     * @since 1.6
     */
    @Nonnull
    public EnumSet<AnalysisStage> getStagesWithExceptions() {
        return this.analyzedCode.getStagesWithExceptions();
    }

}
