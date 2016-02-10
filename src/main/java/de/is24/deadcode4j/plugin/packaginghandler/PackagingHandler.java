package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static de.is24.deadcode4j.Utils.emptyIfNull;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.IOCase.INSENSITIVE;
import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;

/**
 * A <code>PackagingHandler</code> determines which code repositories exist for a specific packaging (like "jar", "war", etc.).
 *
 * @since 1.2.0
 */
public abstract class PackagingHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Returns the "output" repository - i.e. the directory where compiled classes can be found - for the given project.
     *
     * @since 1.2
     */
    @Nullable
    public abstract Repository getOutputRepositoryFor(@Nonnull MavenProject project) throws MojoExecutionException;

    /**
     * Returns additional repositories (configuration, JSPs, raw java files) to analyze for the given project.
     *
     * @since 2.0.0
     */
    @Nonnull
    public Iterable<Repository> getAdditionalRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException {
        return emptyList();
    }

    /**
     * Returns each compile source root of a given <code>MavenProject</code> as a <code>Repository</code> instance
     * providing access to the Java files it contains.
     * Silently ignores compile source roots that do not exist in the file system.
     *
     * @since 2.0.0
     */
    @Nonnull
    protected Collection<Repository> getJavaFilesOfCompileSourceRootsAsRepositories(@Nonnull MavenProject project) {
        Collection<Repository> codeRepositories = newArrayList();
        for (String compileSourceRoot : emptyIfNull(project.getCompileSourceRoots())) {
            File compileSourceDirectory = new File(compileSourceRoot);
            if (!compileSourceDirectory.exists()) {
                logger.debug("  Compile Source Directory [{}] does not exist?", compileSourceDirectory);
                continue;
            }
            codeRepositories.add(new Repository(compileSourceDirectory,
                    new OrFileFilter(DIRECTORY, new RegexFileFilter(".*\\.java$", INSENSITIVE))));
            logger.debug("  Found source directory [{}].", compileSourceRoot);
        }
        return codeRepositories;
    }

}
