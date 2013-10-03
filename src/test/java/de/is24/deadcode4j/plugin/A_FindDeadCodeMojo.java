package de.is24.deadcode4j.plugin;

import de.is24.deadcode4j.DeadCode;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class A_FindDeadCodeMojo {

    private Log logMock;
    private FindDeadCodeMojo findDeadCodeMojo;

    @Before
    public void setUpMojo() {
        findDeadCodeMojo = new FindDeadCodeMojo();

        logMock = mock(Log.class);
        findDeadCodeMojo.setLog(logMock);
    }

    @Test
    public void logsThatNoDeadCodeWasFound() throws Exception {
        List<String> deadCode = emptyList();
        findDeadCodeMojo.log(new DeadCode(newArrayList("A", "B"), deadCode));

        verifyNumberOfAnalyzedClassesIs(2);
        verifyNoDeadCodeWasFound();
    }

    @Test
    public void logsThatOneDeadClassOfThreeWasFound() throws Exception {
        findDeadCodeMojo.log(new DeadCode(newArrayList("A", "B", "SingleClass"), singleton("SingleClass")));

        verifyNumberOfAnalyzedClassesIs(3);
        verify(logMock).warn("Found 1 unused class(es):");
        verify(logMock).warn("  SingleClass");
    }

    @Test
    public void logsThatAClassWasIgnored() throws Exception {
        findDeadCodeMojo.classesToIgnore = singleton("SingleClass");

        findDeadCodeMojo.log(new DeadCode(singleton("SingleClass"), singleton("SingleClass")));

        verifyNumberOfAnalyzedClassesIs(1);
        verify(logMock).info("Ignoring 1 class(es) which seem(s) to be unused.");
        verifyNoDeadCodeWasFound();
    }

    @Test
    public void logsThatAnIgnoredClassDoesNotExist() throws Exception {
        findDeadCodeMojo.classesToIgnore = singleton("com.acme.Foo");

        List<String> emptyList = emptyList();
        findDeadCodeMojo.log(new DeadCode(emptyList, emptyList));

        verifyNumberOfAnalyzedClassesIs(0);
        verify(logMock).warn("Class [com.acme.Foo] should be ignored, but is not dead. You should remove the configuration entry.");
        verifyNoDeadCodeWasFound();
    }

    private void verifyNumberOfAnalyzedClassesIs(int count) {
        verify(logMock).info("Analyzed " + count + " class(es).");
    }

    private void verifyNoDeadCodeWasFound() {
        verify(logMock).info("No unused classes found. Rejoice!");
    }

}
