package de.is24.deadcode4j.plugin.packaginghandler;

import com.google.common.base.Function;
import de.is24.deadcode4j.Repository;
import org.apache.commons.io.IOCase;
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
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
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
     * @since 1.6
     */
    @Nonnull
    public Iterable<Repository> getAdditionalRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException {
        return emptyList();
    }

    /**
     * Adds each compile source root of a given <code>MavenProject</code> as a <code>CodeRepository</code> instance
     * providing access to the Java files it contains.
     *
     * @since 1.6
     */
    protected void addJavaFilesOfSourceDirectories(@Nonnull Collection<Repository> repositories, @Nonnull MavenProject project) {
        repositories.addAll(getJavaFilesOfCompileSourceRootsAsCodeRepositories(project));
    }

    @Nonnull
    private Collection<Repository> getJavaFilesOfCompileSourceRootsAsCodeRepositories(@Nonnull MavenProject project) {
        return new Function<MavenProject, Collection<Repository>>() {
            @Nonnull
            @Override
            public Collection<Repository> apply(@Nullable MavenProject input) {
                if (input == null) {
                    return emptyList();
                }

                List<String> compileSourceRoots = input.getCompileSourceRoots();
                if (compileSourceRoots == null) {
                    return emptyList();
                }

                Collection<Repository> codeRepositories = newArrayList();
                for (String compileSourceRoot : compileSourceRoots) {
                    File compileSourceDirectory = new File(compileSourceRoot);
                    if (!compileSourceDirectory.exists()) {
                        continue;
                    }
                    codeRepositories.add(new Repository(compileSourceDirectory,
                            new OrFileFilter(DIRECTORY, new RegexFileFilter(".*\\.java$", IOCase.INSENSITIVE))));
                    logger.debug("Going to analyze Java files of source directory [{}].", compileSourceRoot);
                }
                return codeRepositories;
            }
        }.apply(project);
    }

}
