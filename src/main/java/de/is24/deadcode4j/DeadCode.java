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
    private final EnumSet<AnalysisStage> stagesWithExceptions;
    @Nonnull
    private final Collection<String> analyzedClasses;
    @Nonnull
    private final Collection<String> deadClasses;

    public DeadCode(@Nonnull EnumSet<AnalysisStage> stagesWithExceptions,
                    @Nonnull Collection<String> analyzedClasses,
                    @Nonnull Collection<String> deadClasses) {
        this.stagesWithExceptions = stagesWithExceptions;
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

    /**
     * Returns the stages for which an exception occurred.
     *
     * @since 2.0.0
     */
    @Nonnull
    public EnumSet<AnalysisStage> getStagesWithExceptions() {
        return stagesWithExceptions;
    }

}
