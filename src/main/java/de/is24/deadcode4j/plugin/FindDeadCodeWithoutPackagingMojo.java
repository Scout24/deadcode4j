package de.is24.deadcode4j.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

/**
 * This goal is deprecated; use <code>find-only</code> instead.<br/>
 * Finds dead (i.e. unused) code. In contrast to <code>find</code>, no phase is executed.
 *
 * @see FindDeadCodeMojo
 * @see FindDeadCodeOnlyMojo
 * @since 1.4
 * @deprecated since 1.5; use {@link FindDeadCodeOnlyMojo} instead
 */
@Mojo(name = "find-without-packaging",
        aggregator = true,
        requiresProject = true,
        requiresDependencyCollection = COMPILE,
        threadSafe = true)
@Deprecated
public class FindDeadCodeWithoutPackagingMojo extends FindDeadCodeOnlyMojo {

    @Override
    public void doExecute() throws MojoExecutionException {
        getLog().warn("##############################################################");
        getLog().warn("  This goal is deprecated and will be removed in the future!");
        getLog().warn("  Instead, use the goal [find-only].");
        getLog().warn("##############################################################");
        super.doExecute();
    }

}
