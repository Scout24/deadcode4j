package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import java.util.Collection;

import static de.is24.deadcode4j.Utils.getKeyFor;
import static java.util.Collections.emptyList;

/**
 * The <code>PomPackagingHandler</code> returns no repository, as there's nothing to analyze.
 *
 * @since 1.2.0
 */
public class PomPackagingHandler extends PackagingHandler {

    @Override
    @Nonnull
    public Collection<Repository> getRepositoriesFor(@Nonnull MavenProject project) {
        logger.debug("Project {} has pom packaging, so it is skipped.", getKeyFor(project));
        return emptyList();
    }

}
