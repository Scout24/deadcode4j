package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.Callable;

import static de.is24.deadcode4j.Utils.getKeyFor;
import static java.util.Collections.emptyList;

/**
 * The <code>PomPackagingHandler</code> returns no repository, as there's nothing to analyze.
 *
 * @since 1.2.0
 */
public class PomPackagingHandler extends PackagingHandler {

    public PomPackagingHandler(Callable<Log> logAccessor) {
        super(logAccessor);
    }

    @Override
    @Nonnull
    public Collection<Repository> getRepositoriesFor(@Nonnull MavenProject project) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Project " + getKeyFor(project) + " has pom packaging, so it is skipped.");
        }
        return emptyList();
    }
}
