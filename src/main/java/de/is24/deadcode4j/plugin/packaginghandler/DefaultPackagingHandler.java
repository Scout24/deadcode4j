package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

import static de.is24.deadcode4j.Utils.getKeyFor;

/**
 * The <code>DefaultPackagingHandler</code> returns the default output directory if it exists.
 *
 * @since 1.2.0
 */
public class DefaultPackagingHandler extends PackagingHandler {

    @Nullable
    @Override
    public Repository getOutputRepositoryFor(@Nonnull MavenProject project) throws MojoExecutionException {
        logger.debug("Project {} has {} packaging, looking for output directory...", getKeyFor(project), project.getPackaging());
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        if (!outputDirectory.exists()) {
            logger.warn("The output directory of " + getKeyFor(project) +
                    " does not exist - assuming the project simply has nothing to provide!");
            return null;
        }
        logger.debug("  Found output directory [{}].", outputDirectory);
        return new Repository(outputDirectory);
    }

    @Nonnull
    @Override
    public Iterable<Repository> getAdditionalRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException {
        return getJavaFilesOfCompileSourceRootsAsRepositories(project);
    }

}
