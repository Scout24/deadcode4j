package de.is24.maven.slf4j;

import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

public final class An_AbstractSlf4jMojo {

    @Test
    public void wrapsMavenLogger() throws MojoFailureException, MojoExecutionException {
        Log logMock = mock(Log.class);
        when(logMock.isInfoEnabled()).thenReturn(true);
        AbstractSlf4jMojo objectUnderTest = new AbstractSlf4jMojo() {
            @Override
            protected void doExecute() throws MojoExecutionException, MojoFailureException {
                LoggerFactory.getLogger(getClass()).info("Hello JUnit!");
            }
        };
        objectUnderTest.setLog(logMock);

        objectUnderTest.execute();

        verify(logMock).info("Hello JUnit!");
    }

    @Test
    public void resetsMockEvenWhenExceptionOccurs() {
        Log logMock = mock(Log.class);
        AbstractSlf4jMojo objectUnderTest = new AbstractSlf4jMojo() {
            @Override
            protected void doExecute() throws MojoExecutionException, MojoFailureException {
                throw new MojoFailureException("FAIL");
            }
        };
        objectUnderTest.setLog(logMock);

        try {
            objectUnderTest.execute();
        } catch (AbstractMojoExecutionException ignored) {
        }
        LoggerFactory.getLogger(getClass()).info("Hello JUnit!");

        verifyZeroInteractions(logMock);
    }

}