package de.is24.deadcode4j.junit;

import org.apache.maven.plugin.logging.SystemStreamLog;

public class TestLog extends SystemStreamLog {
    @Override
    public boolean isDebugEnabled() {
        return true;
    }
}
