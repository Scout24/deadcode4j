package de.is24.deadcode4j.plugin;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IT_PuttingItAllTogether {
    @Rule
    public final MojoRule mojoSetUp = new MojoRule();

    private Log logMock;
    private FindDeadCodeMojo findDeadCodeMojo;

    @Before
    public void setUp() throws Exception {
        URL pomUrl = getClass().getClassLoader().getResource("de/is24/deadcode4j/plugin/projects/pom.xml");
        assertNotNull(pomUrl);
        File pom = new File(pomUrl.toURI());
        assertTrue(pom.exists());

        findDeadCodeMojo = (FindDeadCodeMojo) mojoSetUp.lookupMojo("find", pom);
        assertNotNull(findDeadCodeMojo);

        logMock = mock(Log.class);
        findDeadCodeMojo.setLog(logMock);
    }

    @Test
    public void test() throws Exception {
        findDeadCodeMojo.execute();

        verify(logMock).info("Analyzed 23 class(es).");
        verify(logMock).info("Ignoring 1 class(es) which seem(s) to be unused.");
        verify(logMock).warn("Class [com.acme.Foo] should be ignored, but is not dead. You should remove the configuration entry.");
        verify(logMock).warn("Found 1 unused class(es):");
        verify(logMock).warn("  DeadServlet");
    }

}
