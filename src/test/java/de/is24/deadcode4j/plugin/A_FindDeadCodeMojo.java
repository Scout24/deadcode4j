package de.is24.deadcode4j.plugin;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class A_FindDeadCodeMojo extends AbstractMojoTestCase {

    private Log logMock;

    public void test_logsThatNoDeadCodeWasFound() throws Exception {
        FindDeadCodeMojo findDeadCodeMojo = setUpMojoForProject("noDeadCode");

        findDeadCodeMojo.execute();

        verify(logMock).info("Analyzed 2 class(es).");
        verify(logMock).info("No unused classes found. Rejoice!");
    }

    public void test_logsThatOneDeadClassOfThreeWasFound() throws Exception {
        FindDeadCodeMojo findDeadCodeMojo = setUpMojoForProject("oneDeadClassOfThree");

        findDeadCodeMojo.execute();

        verify(logMock).info("Analyzed 3 class(es).");
        verify(logMock).warn("Found 1 unused class(es):");
        verify(logMock).warn("  SingleClass");
    }

    private FindDeadCodeMojo setUpMojoForProject(String project) throws Exception {
        File pom = getTestFile("src/test/resources/projects/" + project + "/pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        FindDeadCodeMojo findDeadCodeMojo = (FindDeadCodeMojo) lookupMojo("find", pom);
        assertNotNull(findDeadCodeMojo);

        logMock = mock(Log.class);
        findDeadCodeMojo.setLog(logMock);

        return findDeadCodeMojo;
    }

}
