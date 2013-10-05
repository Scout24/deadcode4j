package de.is24.deadcode4j.plugin;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IT_PuttingItAllTogether extends AbstractMojoTestCase {

    private Log logMock;
    private FindDeadCodeMojo findDeadCodeMojo;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        File pom = getTestFile("target/test-classes/de/is24/deadcode4j/plugin/projects/pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        findDeadCodeMojo = (FindDeadCodeMojo) lookupMojo("find", pom);
        assertNotNull(findDeadCodeMojo);

        logMock = mock(Log.class);
        findDeadCodeMojo.setLog(logMock);
    }

    public void test() throws Exception {
        findDeadCodeMojo.execute();

        verify(logMock).info("Analyzed 10 class(es).");
        verify(logMock).info("Ignoring 1 class(es) which seem(s) to be unused.");
        verify(logMock).warn("Class [com.acme.Foo] should be ignored, but is not dead. You should remove the configuration entry.");
        verify(logMock).warn("Found 2 unused class(es):");
        verify(logMock).warn("  DeadServlet");
        verify(logMock).warn("  SingleClass");
    }

}
