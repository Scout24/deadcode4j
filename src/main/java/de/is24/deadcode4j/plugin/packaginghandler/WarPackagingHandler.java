package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import de.is24.deadcode4j.plugin.SubDirectoryFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static de.is24.deadcode4j.Utils.getKeyFor;
import static org.apache.commons.io.filefilter.FileFilterUtils.*;

/**
 * The <code>WarPackagingHandler</code> returns the configured <tt>webappDirectory</tt> or the default directory where
 * the webapp is built for "war" packaging.
 *
 * @since 1.2.0
 */
public class WarPackagingHandler extends PackagingHandler {

    @Nullable
    @Override
    public Repository getOutputRepositoryFor(@Nonnull MavenProject project) throws MojoExecutionException {
        final File webAppDirectory = calculateWebAppDirectory(project, true);
        return new Repository(new File(webAppDirectory, "WEB-INF/classes"));
    }

    @Override
    @Nonnull
    public Iterable<Repository> getAdditionalRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException {
        List<Repository> repositories = newArrayList(getJavaFilesOfCompileSourceRootsAsRepositories(project));
        repositories.add(getWebInfDirectory(project));
        return repositories;
    }

    private File calculateWebAppDirectory(MavenProject project, boolean log) throws MojoExecutionException {
        if (log) {
            logger.debug("Project {} has war packaging, looking for webapp directory...", getKeyFor(project));
        }
        Plugin plugin = project.getPlugin("org.apache.maven.plugins:maven-war-plugin");
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        Xpp3Dom webappDirectoryConfig = configuration == null ? null : configuration.getChild("webappDirectory");
        final File webappDirectory;
        if (webappDirectoryConfig != null) {
            webappDirectory = new File(webappDirectoryConfig.getValue());
            if (log) {
                logger.debug("  Found custom webapp directory [{}].", webappDirectory);
            }
        } else {
            webappDirectory = new File(project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName());
            if (log) {
                logger.debug("  Using default webapp directory [{}].", webappDirectory);
            }
        }
        if (!webappDirectory.exists()) {
            throw new MojoExecutionException("The webapp directory of " + getKeyFor(project) +
                    " does not exist - please make sure the project is packaged!");
        }
        return webappDirectory;
    }

    private Repository getWebInfDirectory(@Nonnull MavenProject project) throws MojoExecutionException {
        final File webAppDirectory = calculateWebAppDirectory(project, false);
        final File directory = new File(webAppDirectory, "WEB-INF");
        IOFileFilter fileFilter = notFileFilter(or(
                        asFileFilter(new SubDirectoryFilter(directory, "lib")),
                        asFileFilter(new SubDirectoryFilter(directory, "classes")))
        );
        return new Repository(directory, fileFilter);

    }

}
