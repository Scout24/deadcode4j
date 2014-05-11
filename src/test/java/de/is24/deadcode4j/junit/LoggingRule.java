package de.is24.deadcode4j.junit;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.impl.StaticLoggerBinder;

public class LoggingRule extends TestWatcher {

    @Override
    protected void starting(Description description) {
        StaticLoggerBinder.getSingleton().setLog(new SystemStreamLog() {
            @Override
            public boolean isDebugEnabled() {
                return true;
            }
        });
    }

    @Override
    protected void finished(Description description) {
        StaticLoggerBinder.getSingleton().revokeLog();
    }

}
