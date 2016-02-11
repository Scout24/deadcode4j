package de.is24.deadcode4j.analyzer;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;

@RunWith(JMockit.class)
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
    public void handlesIOExceptionWhenAnalyzingFile() {
        new MockUp<Properties>() {
            @Mock
            public synchronized void load(InputStream inStream) throws IOException {
                throw new IOException("JUnit");
            }
        };

        try {
            analyzeFile(SPRING_HANDLER_FILE);
            fail("Should abort analysis!");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString(SPRING_HANDLER_FILE));
        }
    }

}
