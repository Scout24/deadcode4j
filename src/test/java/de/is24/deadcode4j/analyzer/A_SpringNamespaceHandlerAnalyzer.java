package de.is24.deadcode4j.analyzer;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;

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
    public void handlesException() throws Exception {
        new MockUp<FileInputStream>() {
            @Mock
            public void $init(File file) throws IOException {
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
