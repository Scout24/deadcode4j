package de.is24.deadcode.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * The FindDeadCodeMojo attempts to find unused code.
 *
 * @goal find
 * @execute phase="compile"
 * @threadSafe true
 */
@SuppressWarnings("UnusedDeclaration")
public class FindDeadCodeMojo extends AbstractMojo {
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().warn("I'm not implemented yet!");
    }

}
