package de.is24.deadcode4j.plugin;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

/**
 * Finds dead (i.e. unused) code. Causes the
 * <a href="http://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html">package</a> phase to be
 * executed.
 *
 * @see FindDeadCodeOnlyMojo
 * @since 1.0.0
 */
@Mojo(name = "find",
        aggregator = true,
        requiresProject = true,
        requiresDependencyCollection = COMPILE,
        threadSafe = true)
@Execute(phase = PACKAGE)
public class FindDeadCodeMojo extends FindDeadCodeOnlyMojo {

}
