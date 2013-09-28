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

        verifyNumberOfAnalyzedClassesIs(2);
        verifyNoDeadCodeWasFound();
    }

    public void test_logsThatOneDeadClassOfThreeWasFound() throws Exception {
        FindDeadCodeMojo findDeadCodeMojo = setUpMojoForProject("oneDeadClassOfThree");

        findDeadCodeMojo.execute();

        verifyNumberOfAnalyzedClassesIs(3);
        verify(logMock).warn("Found 1 unused class(es):");
        verify(logMock).warn("  SingleClass");
    }

    public void test_logsThatAClassWasIgnored() throws Exception {
        FindDeadCodeMojo findDeadCodeMojo = setUpMojoForProject("ignoredClass");

        findDeadCodeMojo.execute();

        verifyNumberOfAnalyzedClassesIs(1);
        verify(logMock).info("Ignoring 1 class(es) which seem(s) to be unused.");
        verifyNoDeadCodeWasFound();
    }

    public void test_logsThatAnIgnoredClassDoesNotExist() throws Exception {
        FindDeadCodeMojo findDeadCodeMojo = setUpMojoForProject("unknownIgnoredClass");

        findDeadCodeMojo.execute();

        verifyNumberOfAnalyzedClassesIs(0);
        verify(logMock).warn("Class [com.acme.Foo] should be ignored, but is not dead. You should remove the configuration entry.");
        verifyNoDeadCodeWasFound();
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

    private void verifyNumberOfAnalyzedClassesIs(int count) {
        verify(logMock).info("Analyzed " + count + " class(es).");
    }

    private void verifyNoDeadCodeWasFound() {
        verify(logMock).info("No unused classes found. Rejoice!");
    }

}
