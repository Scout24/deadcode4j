package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static java.util.Collections.emptyList;

/**
 * A <code>PackagingHandler</code> determines which code repositories exist for a specific packaging (like "jar", "war", etc.).
 *
 * @since 1.2.0
 */
public abstract class PackagingHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Returns the "output" repository - i.e. the directory where compiled classes can be found - for the given project.
     *
     * @since 1.2
     */
    @Nullable
    public abstract Repository getOutputRepositoryFor(@Nonnull MavenProject project) throws MojoExecutionException;

    /**
     * Returns additional repositories (configuration, JSPs, raw java files) to analyze for the given project.
     *
     * @since 1.6
     */
    @Nonnull
    public Collection<Repository> getAdditionalRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException {
        return emptyList();
    }

}
