package de.is24.deadcode4j.junit;

import com.google.common.base.Strings;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.impl.StaticLoggerBinder;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class LoggingRule extends TestWatcher {

    private final Log log;

    public LoggingRule(Log log) {
        this.log = log;
    }

    public LoggingRule() {
        this(new DefaultLog(new ConsoleLogger(Logger.LEVEL_DEBUG, "junit")));
    }

    public static Log createMock() {
        Log logMock = mock(Log.class);
        when(logMock.isDebugEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("DEBUG")).when(logMock).debug(any(CharSequence.class));
        doAnswer(new LogAnswer("DEBUG")).when(logMock).debug(any(CharSequence.class), any(Throwable.class));
        doAnswer(new LogAnswer("DEBUG")).when(logMock).debug(any(Throwable.class));
        when(logMock.isInfoEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("INFO")).when(logMock).info(any(CharSequence.class));
        doAnswer(new LogAnswer("INFO")).when(logMock).info(any(CharSequence.class), any(Throwable.class));
        doAnswer(new LogAnswer("INFO")).when(logMock).info(any(Throwable.class));
        when(logMock.isWarnEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("WARN")).when(logMock).warn(any(CharSequence.class));
        doAnswer(new LogAnswer("WARN")).when(logMock).warn(any(CharSequence.class), any(Throwable.class));
        doAnswer(new LogAnswer("WARN")).when(logMock).warn(any(Throwable.class));
        when(logMock.isErrorEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("ERROR")).when(logMock).error(any(CharSequence.class));
        doAnswer(new LogAnswer("ERROR")).when(logMock).error(any(CharSequence.class), any(Throwable.class));
        doAnswer(new LogAnswer("ERROR")).when(logMock).error(any(Throwable.class));
        return logMock;
    }

    @Override
    protected void starting(Description description) {
        StaticLoggerBinder.getSingleton().setLog(log);
    }

    @Override
    protected void finished(Description description) {
        StaticLoggerBinder.getSingleton().revokeLog();
    }

    private static class LogAnswer implements Answer<Void> {

        private final String level;

        public LogAnswer(String level) {
            this.level = level;
        }

        @Override
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            final Object[] arguments = invocationOnMock.getArguments();
            final String message;
            Throwable throwable = null;
            if (Throwable.class.isInstance(arguments[0])) {
                throwable = Throwable.class.cast(arguments[0]);
                message = throwable.getMessage();
            } else {
                message = String.valueOf(arguments[0]);
            }
            if (arguments.length > 1) {
                throwable = Throwable.class.cast(arguments[1]);
            }
            System.out.println("[" + Strings.padEnd(level, 5, ' ') + "] " + message);
            if (throwable != null) {
                throwable.printStackTrace(System.out);
            }
            return null;
        }

    }

}
