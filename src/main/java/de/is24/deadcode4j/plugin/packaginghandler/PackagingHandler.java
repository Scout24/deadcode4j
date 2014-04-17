package de.is24.deadcode4j.plugin.packaginghandler;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import de.is24.deadcode4j.CodeRepository;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;

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

    /**
     * Adds each compile source root of a given <code>MavenProject</code> as a <code>CodeRepository</code> instance
     * providing access to the Java files it contains.
     *
     * @since 1.6
     */
    protected void addJavaFilesOfSourceDirectories(@Nonnull Collection<CodeRepository> repositories, @Nonnull MavenProject project) {
        repositories.addAll(getJavaFilesOfCompileSourceRootsAsCodeRepositories(project));
    }

    @Nonnull
    private Collection<CodeRepository> getJavaFilesOfCompileSourceRootsAsCodeRepositories(@Nonnull MavenProject project) {
        return new Function<MavenProject, Collection<CodeRepository>>() {
            @Nonnull
            @Override
            public Collection<CodeRepository> apply(@Nullable MavenProject input) {
                if (input == null) {
                    return emptyList();
                }

                List<String> compileSourceRoots = input.getCompileSourceRoots();
                if (compileSourceRoots == null) {
                    return emptyList();
                }

                Collection<CodeRepository> codeRepositories = newArrayList();
                for (String compileSourceRoot : compileSourceRoots) {
                    codeRepositories.add(new CodeRepository(new File(compileSourceRoot),
                            new OrFileFilter(DIRECTORY, new RegexFileFilter(".*\\.java$", IOCase.INSENSITIVE))));
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Going to analyze Java files of source directory [" + compileSourceRoot + "].");
                    }
                }
                return codeRepositories;
            }
        }.apply(project);
    }

}
