package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;

import static de.is24.deadcode4j.Utils.getKeyFor;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * The <code>DefaultPackagingHandler</code> returns the default output directory if it exists.
 *
 * @since 1.2.0
 */
public class DefaultPackagingHandler extends PackagingHandler {

    @Override
    @Nonnull
    public Collection<Repository> getRepositoriesFor(@Nonnull MavenProject project) {
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        if (!outputDirectory.exists()) {
            logger.warn("The output directory of " + getKeyFor(project) +
                    " does not exist - assuming the project simply has nothing to provide!");
            return emptyList();
        }
        logger.debug("Going to analyze output directory [{}].", outputDirectory);
        return singleton(new Repository(outputDirectory));
    }

}
