package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import de.is24.deadcode4j.plugin.SubDirectoryFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;

import static de.is24.deadcode4j.Utils.getKeyFor;
import static java.util.Collections.singleton;
import static org.apache.commons.io.filefilter.FileFilterUtils.asFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;

/**
 * The <code>WarPackagingHandler</code> returns the configured <tt>webappDirectory</tt> or the default directory where
 * the webapp is built for "war" packaging.
 *
 * @since 1.2.0
 */
public class WarPackagingHandler extends PackagingHandler {

    @Override
    @Nonnull
    public Collection<Repository> getRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException {
        logger.debug("Project {} has war packaging, looking for webapp directory...", getKeyFor(project));
        Plugin plugin = project.getPlugin("org.apache.maven.plugins:maven-war-plugin");
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        Xpp3Dom webappDirectoryConfig = configuration == null ? null : configuration.getChild("webappDirectory");
        final File webappDirectory;
        if (webappDirectoryConfig != null) {
            webappDirectory = new File(webappDirectoryConfig.getValue());
            logger.debug("Found custom webapp directory [{}].", webappDirectory);
        } else {
            webappDirectory = new File(project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName());
            logger.debug("Using default webapp directory [{}].", webappDirectory);
        }
        if (!webappDirectory.exists()) {
            throw new MojoExecutionException("The webapp directory of " + getKeyFor(project) +
                    " does not exist - please make sure the project is packaged!");
        }
        final File directory = new File(webappDirectory, "WEB-INF");
        IOFileFilter fileFilter = notFileFilter(asFileFilter(new SubDirectoryFilter(directory, "lib")));
        return singleton(new Repository(directory, fileFilter));
    }

}
