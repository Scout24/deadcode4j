package org.slf4j.impl;

import de.is24.maven.slf4j.MavenPluginLoggerFactory;
import org.apache.maven.plugin.logging.Log;
import org.slf4j.ILoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.spi.LoggerFactoryBinder;

import javax.annotation.Nonnull;

/**
 * Binds slf4j's <code>LoggerFactory</code> to {@link de.is24.maven.slf4j.MavenPluginLoggerFactory}.
 *
 * @author <a href="https://github.com/sebastiankirsch">Sebastian Kirsch</a>
 * @since 1.5
 */
public final class StaticLoggerBinder implements LoggerFactoryBinder {

    private static final StaticLoggerBinder INSTANCE = new StaticLoggerBinder();

    private final ThreadLocal<ILoggerFactory> loggerFactoryForThread = new ThreadLocal<ILoggerFactory>();

    private StaticLoggerBinder() {
        super();
    }

    @Nonnull
    public static StaticLoggerBinder getSingleton() {
        return INSTANCE;
    }

    /**
     * Returns a <code>MavenPluginLoggerFactory</code> if the current thread was properly
     * {@link #setLog(org.apache.maven.plugin.logging.Log) initialized}; an instance of
     * <code>NOPLoggerFactory</code> otherwise.
     *
     * @since 1.5
     */
    @Nonnull
    @Override
    public ILoggerFactory getLoggerFactory() {
        ILoggerFactory loggerFactory = loggerFactoryForThread.get();
        if (loggerFactory == null) {
            Util.report("No Maven Log set; using NOPLoggerFactory! " +
                    "Make sure to call StaticLoggerBinder.getSingleton().setLog(Log log)!");
            loggerFactory = new NOPLoggerFactory();
        }
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return MavenPluginLoggerFactory.class.getName();
    }

    /**
     * Sets up a <code>MavenPluginLoggerFactory</code> for the current thread.
     * Make sure to {@link #revokeLog() revoke the Log} after execution.
     *
     * @since 1.5
     */
    public void setLog(@Nonnull final Log log) {
        this.loggerFactoryForThread.set(new MavenPluginLoggerFactory(log));
    }

    /**
     * Tears down the <code>MavenPluginLoggerFactory</code> bound to the current thread.
     *
     * @since 1.5
     */
    public void revokeLog() {
        this.loggerFactoryForThread.remove();
    }

}
