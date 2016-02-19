package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisSink;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;

@PrepareForTest({SpringNamespaceHandlerAnalyzer.class})
@RunWith(PowerMockRunner.class)
public final class A_SpringNamespaceHandlerAnalyzer extends AnAnalyzer<SpringNamespaceHandlerAnalyzer> {

    private static final String SPRING_HANDLER_FILE = "META-INF/spring.handlers";

    @Override
    protected SpringNamespaceHandlerAnalyzer createAnalyzer() {
        return new SpringNamespaceHandlerAnalyzer();
    }

    @Test
    public void shouldRecognizeDefinedNamespaceHandlers() {
        analyzeFile(SPRING_HANDLER_FILE);

        assertThatDependenciesAreReported("CustomNamespaceHandler", "AnotherNamespaceHandler");
    }

    @Test
    public void handlesIOExceptionWhenAnalyzingFile() throws Exception {
        Properties mock = Mockito.mock(Properties.class);
        doThrow(new IOException("JUnit")).when(mock).load(Mockito.any(InputStream.class));
        PowerMockito.whenNew(Properties.class).withNoArguments().thenReturn(mock);

        try {
            analyzeFile(SPRING_HANDLER_FILE);
            fail("Should abort analysis!");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString(SPRING_HANDLER_FILE));
        }
    }

}
