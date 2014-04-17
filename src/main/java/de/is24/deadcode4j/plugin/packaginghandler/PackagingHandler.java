package de.is24.deadcode4j.plugin.packaginghandler;

import com.google.common.base.Preconditions;
import de.is24.deadcode4j.CodeRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * A <code>PackagingHandler</code> determines which code repositories exist for a specific packaging (like "jar", "war", etc.).
 *
 * @since 1.2.0
 */
public abstract class PackagingHandler {

    private final Callable<Log> logAccessor;

    protected PackagingHandler(Callable<Log> logAccessor) {
        Preconditions.checkNotNull(logAccessor);
        this.logAccessor = logAccessor;
    }

    /**
     * Returns the code repositories to analyze for this packaging.
     *
     * @since 1.2.0
     */
    @Nonnull
    public abstract Collection<CodeRepository> getCodeRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException;

    /**
     * Returns the {@link org.apache.maven.plugin.logging.Log} to use.
     *
     * @since 1.6
     */
    protected final Log getLog() {
        try {
            return logAccessor.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
