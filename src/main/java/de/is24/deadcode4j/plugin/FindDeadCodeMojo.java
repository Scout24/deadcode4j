package de.is24.deadcode4j.plugin;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;

/**
 * Finds dead (i.e. unused) code. Causes the
 * <a href="http://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html">package</a> phase to be
 * executed.
 *
 * @see FindDeadCodeWithoutPackagingMojo
 * @since 1.0.0
 */
@Mojo(name = "find", aggregator = true, threadSafe = true, requiresProject = true)
@Execute(phase = PACKAGE)
@SuppressWarnings("PMD.TooManyStaticImports")
public class FindDeadCodeMojo extends FindDeadCodeWithoutPackagingMojo {

}
