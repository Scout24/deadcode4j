package de.is24.maven.slf4j;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An <code>ILoggerFactory</code> creating instances of {@link LoggerForMavenLog}.
 *
 * @author <a href="https://github.com/sebastiankirsch">Sebastian Kirsch</a>
 * @since 1.5
 */
public class MavenPluginLoggerFactory implements ILoggerFactory {
    private final Log log;

    public MavenPluginLoggerFactory(@Nonnull Log log) {
        this.log = log;
    }

    @Override
    public Logger getLogger(@Nullable String name) {
        return new LoggerForMavenLog(log, name);
    }

}
