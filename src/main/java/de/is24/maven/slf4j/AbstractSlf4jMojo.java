package de.is24.maven.slf4j;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * A subclass of <code>AbstractMojo</code> that makes sure the {@link MavenPluginLoggerFactory} is set up and torn down.
 *
 * @author <a href="https://github.com/sebastiankirsch">Sebastian Kirsch</a>
 * @since 1.5
 */
public abstract class AbstractSlf4jMojo extends AbstractMojo {
    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        StaticLoggerBinder staticLoggerBinder = StaticLoggerBinder.getSingleton();
        staticLoggerBinder.setLog(getLog());
        try {
            doExecute();
        } finally {
            staticLoggerBinder.revokeLog();
        }
    }

    /**
     * Subclasses need to implement this method instead of {@link org.apache.maven.plugin.Mojo#execute()}.
     *
     * @since 1.5
     */
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

}
