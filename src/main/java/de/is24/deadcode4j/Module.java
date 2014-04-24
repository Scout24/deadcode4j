package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * A <code>Module</code> represents a java module from the deadcode4j perspective.
 *
 * @since 1.6
 */
public class Module {

    private final List<Repository> codeRepositories;

    /**
     * Creates a new <code>Module</code>.
     *
     * @param repositories a <code>Collection</code> containing the module's repositories to analyze
     * @since 1.6
     */
    public Module(@Nonnull Collection<Repository> repositories) {
        this.codeRepositories = newArrayList(repositories);
    }

    /**
     * Returns all repositories to analyze.
     *
     * @since 1.6
     */
    @Nonnull
    public Iterable<Repository> getAllRepositories() {
        return this.codeRepositories;
    }

}
