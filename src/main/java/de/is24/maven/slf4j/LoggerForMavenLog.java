package de.is24.maven.slf4j;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;
import static org.slf4j.helpers.MessageFormatter.format;

/**
 * An slf4j <code>Logger</code> backed by a Maven Plugin <code>Log</code>.
 *
 * @author <a href="https://github.com/sebastiankirsch">Sebastian Kirsch</a>
 * @since 1.5
 */
public class LoggerForMavenLog extends MarkerIgnoringBase {

    private final Log log;

    public LoggerForMavenLog(@Nonnull Log log, @Nullable String name) {
        this.log = log;
        this.name = name;
    }

    private void doDebug(String message, Throwable throwable) {
        String renderedMessage = this.name == null ? message : this.name + ": " + message;
        if (throwable == null) {
            log.debug(renderedMessage);
        } else {
            log.debug(renderedMessage, throwable);
        }
    }

    private void doDebug(String message) {
        doDebug(message, null);
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void trace(String msg) {
        if (log.isDebugEnabled()) {
            doDebug(msg);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (log.isDebugEnabled()) {
            doDebug(format(format, arg).getMessage());
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (log.isDebugEnabled()) {
            FormattingTuple tuple = format(format, arg1, arg2);
            doDebug(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (log.isDebugEnabled()) {
            FormattingTuple tuple = arrayFormat(format, arguments);
            doDebug(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (log.isDebugEnabled()) {
            doDebug(msg, t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        if (log.isDebugEnabled()) {
            doDebug(msg);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (log.isDebugEnabled()) {
            doDebug(format(format, arg).getMessage());
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (log.isDebugEnabled()) {
            FormattingTuple tuple = format(format, arg1, arg2);
            doDebug(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (log.isDebugEnabled()) {
            FormattingTuple tuple = arrayFormat(format, arguments);
            doDebug(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (log.isDebugEnabled()) {
            doDebug(msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        if (log.isInfoEnabled()) {
            log.info(msg);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (log.isInfoEnabled()) {
            log.info(format(format, arg).getMessage());
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (log.isInfoEnabled()) {
            FormattingTuple tuple = format(format, arg1, arg2);
            log.info(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (log.isInfoEnabled()) {
            FormattingTuple tuple = arrayFormat(format, arguments);
            log.info(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (log.isInfoEnabled()) {
            log.info(msg, t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        if (log.isWarnEnabled()) {
            log.warn(msg);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (log.isWarnEnabled()) {
            log.warn(format(format, arg).getMessage());
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (log.isWarnEnabled()) {
            FormattingTuple tuple = format(format, arg1, arg2);
            log.warn(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (log.isWarnEnabled()) {
            FormattingTuple tuple = arrayFormat(format, arguments);
            log.warn(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (log.isWarnEnabled()) {
            log.warn(msg, t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        log.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        log.error(format(format, arg).getMessage());
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        FormattingTuple tuple = format(format, arg1, arg2);
        log.error(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void error(String format, Object... arguments) {
        FormattingTuple tuple = arrayFormat(format, arguments);
        log.error(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void error(String msg, Throwable t) {
        log.error(msg, t);
    }

}
