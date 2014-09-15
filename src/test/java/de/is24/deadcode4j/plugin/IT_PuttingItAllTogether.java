package de.is24.deadcode4j.plugin;

import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class IT_PuttingItAllTogether {
    @Rule
    public final MojoRule mojoSetUp = new MojoRule();

    private Log logMock;
    private FindDeadCodeMojo findDeadCodeMojo;

    @Test
    public void isConfiguredAndAnalyzesClassesAsExpected() throws Exception {
        setUpMojo("de/is24/deadcode4j/plugin/projects/pom.xml");

        findDeadCodeMojo.execute();

        verify(logMock).info("Analyzed 26 class(es).");
        verify(logMock).info("Ignoring 1 class(es) which seem(s) to be unused.");
        verify(logMock).warn("Class [com.acme.Foo] should be ignored, but does not exist. You should remove the configuration entry.");
        verify(logMock).warn("Found 3 unused class(es):");
        verify(logMock).warn("  ClassWithTypeArgument");
        verify(logMock).warn("  DeadServlet");
        verify(logMock).warn("  SomeServletInitializer"); // this doesn't work because the classpath is faked
    }

    @Test
    public void handlesRuntimeException() throws Exception {
        setUpMojo("de/is24/deadcode4j/plugin/projects/misconfig.pom.xml");

        try {
            findDeadCodeMojo.execute();
            fail("Should throw an exception");
        } catch (IllegalArgumentException ignored) {
        }
        verify(logMock).error(eq("An unexpected exception occurred. " +
                "Please consider reporting an issue at https://github.com/ImmobilienScout24/deadcode4j/issues"), any(IllegalArgumentException.class));
    }

    private void setUpMojo(String pomFile) throws Exception {
        URL pomUrl = getClass().getClassLoader().getResource(pomFile);
        assertNotNull(pomUrl);
        File pom = new File(pomUrl.toURI());
        assertTrue(pom.exists());

        findDeadCodeMojo = (FindDeadCodeMojo) mojoSetUp.lookupMojo("find", pom);
        assertNotNull(findDeadCodeMojo);

        logMock = LoggingRule.createMock();
        findDeadCodeMojo.setLog(logMock);
    }

}
