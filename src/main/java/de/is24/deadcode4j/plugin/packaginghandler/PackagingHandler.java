package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A <code>PackagingHandler</code> determines which code repositories exist for a specific packaging (like "jar", "war", etc.).
 *
 * @since 1.2.0
 */
public abstract class PackagingHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Returns the code repositories to analyze for this packaging.
     *
     * @since 1.2.0
     */
    @Nonnull
    public abstract Collection<Repository> getRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException;

}
