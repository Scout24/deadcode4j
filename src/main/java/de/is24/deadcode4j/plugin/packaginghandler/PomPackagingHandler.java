package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static de.is24.deadcode4j.Utils.getKeyFor;

/**
 * The <code>PomPackagingHandler</code> returns no repository, as there's nothing to analyze.
 *
 * @since 1.2.0
 */
public class PomPackagingHandler extends PackagingHandler {

    @Nullable
    @Override
    public Repository getOutputRepositoryFor(@Nonnull MavenProject project) throws MojoExecutionException {
        logger.debug("Project {} has pom packaging, so it is skipped.", getKeyFor(project));
        return null;
    }

}
