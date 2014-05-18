package de.is24.deadcode4j.junit;

import com.google.common.base.Strings;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
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
        this(new SystemStreamLog() {
            @Override
            public boolean isDebugEnabled() {
                return true;
            }
        });
    }

    public static Log createMock() {
        Log logMock = mock(Log.class);
        when(logMock.isDebugEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("DEBUG")).when(logMock).debug(any(CharSequence.class));
        when(logMock.isInfoEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("INFO")).when(logMock).info(any(CharSequence.class));
        when(logMock.isWarnEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("WARN")).when(logMock).warn(any(CharSequence.class));
        when(logMock.isErrorEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("ERROR")).when(logMock).error(any(CharSequence.class));
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
        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            System.out.println("[" + Strings.padEnd(level, 5, ' ') + "] " + invocationOnMock.getArguments()[0]);
            return null;
        }

    }

}
