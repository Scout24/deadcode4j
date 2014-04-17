package de.is24.deadcode4j.junit;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.impl.StaticLoggerBinder;

public class LoggingRule extends TestWatcher {

    @Override
    protected void starting(Description description) {
        StaticLoggerBinder.getSingleton().setLog(new TestLog());
    }

    @Override
    protected void finished(Description description) {
        StaticLoggerBinder.getSingleton().revokeLog();
    }

}
