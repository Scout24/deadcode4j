package de.is24.maven.slf4j;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class A_LoggerForMavenLog {

    private Log logMock;
    private LoggerForMavenLog objectUnderTest;

    @Before
    public void setUp() throws Exception {
        logMock = Mockito.mock(Log.class);
        objectUnderTest = new LoggerForMavenLog(logMock, "JUnitLog");
    }

    @Test
    public void shouldDelegateIsLevelEnabledMethods() {
        when(logMock.isDebugEnabled()).thenReturn(false).thenReturn(true).thenReturn(true).thenReturn(false);
        when(logMock.isInfoEnabled()).thenReturn(false).thenReturn(true);
        when(logMock.isWarnEnabled()).thenReturn(true).thenReturn(false);
        when(logMock.isErrorEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(objectUnderTest.isTraceEnabled());
        assertTrue(objectUnderTest.isTraceEnabled());
        assertTrue(objectUnderTest.isDebugEnabled());
        assertFalse(objectUnderTest.isDebugEnabled());
        assertFalse(objectUnderTest.isInfoEnabled());
        assertTrue(objectUnderTest.isInfoEnabled());
        assertTrue(objectUnderTest.isWarnEnabled());
        assertFalse(objectUnderTest.isWarnEnabled());
        assertFalse(objectUnderTest.isErrorEnabled());
        assertTrue(objectUnderTest.isErrorEnabled());
    }

    @Test
    public void handlesTraceMethods() {
        objectUnderTest = new LoggerForMavenLog(logMock, null);
        when(logMock.isDebugEnabled()).thenReturn(false);
        objectUnderTest.trace("No log");
        objectUnderTest.trace("No log 2", new IllegalArgumentException());
        objectUnderTest.trace("No log {}", 3);
        objectUnderTest.trace("No log {}{}", 4, 5);
        objectUnderTest.trace("No log {}{}", 6, 7, new NullPointerException());
        when(logMock.isDebugEnabled()).thenReturn(true);
        objectUnderTest.trace("1");
        objectUnderTest.trace("2", new IllegalArgumentException());
        objectUnderTest.trace("{}", 3);
        objectUnderTest.trace("{}{}", 4, 5);
        objectUnderTest.trace("{}{}", 6, 7, new NullPointerException());

        verify(logMock).debug(eq("1"));
        verify(logMock).debug(eq("2"), isA(IllegalArgumentException.class));
        verify(logMock).debug(eq("3"));
        verify(logMock).debug(eq("45"));
        verify(logMock).debug(eq("67"), isA(NullPointerException.class));
    }

    @Test
    public void handlesDebugMethods() {
        when(logMock.isDebugEnabled()).thenReturn(false);
        objectUnderTest.debug("No log");
        objectUnderTest.debug("No log 2", new IllegalArgumentException());
        objectUnderTest.debug("No log {}", 3);
        objectUnderTest.debug("No log {}{}", 4, 5);
        objectUnderTest.debug("No log {}{}", 6, 7, new NullPointerException());
        when(logMock.isDebugEnabled()).thenReturn(true);
        objectUnderTest.debug("1");
        objectUnderTest.debug("2", new IllegalArgumentException());
        objectUnderTest.debug("{}", 3);
        objectUnderTest.debug("{}{}", 4, 5);
        objectUnderTest.debug("{}{}", 6, 7, new NullPointerException());

        verify(logMock).debug(eq("JUnitLog: 1"));
        verify(logMock).debug(eq("JUnitLog: 2"), isA(IllegalArgumentException.class));
        verify(logMock).debug(eq("JUnitLog: 3"));
        verify(logMock).debug(eq("JUnitLog: 45"));
        verify(logMock).debug(eq("JUnitLog: 67"), isA(NullPointerException.class));
    }

    @Test
    public void handlesInfoMethods() {
        when(logMock.isInfoEnabled()).thenReturn(false);
        objectUnderTest.info("No log");
        objectUnderTest.info("No log 2", new IllegalArgumentException());
        objectUnderTest.info("No log {}", 3);
        objectUnderTest.info("No log {}{}", 4, 5);
        objectUnderTest.info("No log {}{}", 6, 7, new NullPointerException());
        when(logMock.isInfoEnabled()).thenReturn(true);
        objectUnderTest.info("1");
        objectUnderTest.info("2", new IllegalArgumentException());
        objectUnderTest.info(null, new IllegalStateException());
        objectUnderTest.info("{}", 3);
        objectUnderTest.info("{}{}", 4, 5);
        objectUnderTest.info("{}{}", 6, 7, new NullPointerException());

        verify(logMock).info(eq("1"));
        verify(logMock).info(eq("2"), isA(IllegalArgumentException.class));
        verify(logMock).info(isNull(String.class), isA(IllegalStateException.class));
        verify(logMock).info(eq("3"));
        verify(logMock).info(eq("45"));
        verify(logMock).info(eq("67"), isA(NullPointerException.class));
    }

    @Test
    public void handlesWarnMethods() {
        when(logMock.isWarnEnabled()).thenReturn(false);
        objectUnderTest.warn("No log");
        objectUnderTest.warn("No log 2", new IllegalArgumentException());
        objectUnderTest.warn("No log {}", 3);
        objectUnderTest.warn("No log {}{}", 4, 5);
        objectUnderTest.warn("No log {}{}", 6, 7, new NullPointerException());
        when(logMock.isWarnEnabled()).thenReturn(true);
        objectUnderTest.warn("1");
        objectUnderTest.warn("2", new IllegalArgumentException());
        objectUnderTest.warn(null, new IllegalStateException());
        objectUnderTest.warn("{}", 3);
        objectUnderTest.warn("{}{}", 4, 5);
        objectUnderTest.warn("{}{}", 6, 7, new NullPointerException());

        verify(logMock).warn(eq("1"));
        verify(logMock).warn(eq("2"), isA(IllegalArgumentException.class));
        verify(logMock).warn(isNull(String.class), isA(IllegalStateException.class));
        verify(logMock).warn(eq("3"));
        verify(logMock).warn(eq("45"));
        verify(logMock).warn(eq("67"), isA(NullPointerException.class));
    }

    @Test
    public void handlesErrorMethods() {
        when(logMock.isErrorEnabled()).thenReturn(false);
        objectUnderTest.error("No log");
        objectUnderTest.error("No log 2", new IllegalArgumentException());
        objectUnderTest.error("No log {}", 3);
        objectUnderTest.error("No log {}{}", 4, 5);
        objectUnderTest.error("No log {}{}", 6, 7, new NullPointerException());
        when(logMock.isErrorEnabled()).thenReturn(true);
        objectUnderTest.error("1");
        objectUnderTest.error("2", new IllegalArgumentException());
        objectUnderTest.error(null, new IllegalStateException());
        objectUnderTest.error("{}", 3);
        objectUnderTest.error("{}{}", 4, 5);
        objectUnderTest.error("{}{}", 6, 7, new NullPointerException());

        verify(logMock).error(eq("1"));
        verify(logMock).error(eq("2"), isA(IllegalArgumentException.class));
        verify(logMock).error(isNull(String.class), isA(IllegalStateException.class));
        verify(logMock).error(eq("3"));
        verify(logMock).error(eq("45"));
        verify(logMock).error(eq("67"), isA(NullPointerException.class));
    }

}