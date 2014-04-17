package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.CodeRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A <code>PackagingHandler</code> determines which code repositories exist for a specific packaging (like "jar", "war", etc.).
 */
public abstract class PackagingHandler {
    @Nonnull
    public abstract Collection<CodeRepository> getCodeRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException;
}
