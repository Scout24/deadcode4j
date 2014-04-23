package de.is24.deadcode4j;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static java.io.File.createTempFile;

@SuppressWarnings("ConstantConditions")
public class A_CodeRepository {

    @Test(expected = NullPointerException.class)
    public void throwsAnExceptionIfTheRepositoryIsNull() {
        new CodeRepository(Collections.<File>emptyList(), null);
    }

    @Test(expected = NullPointerException.class)
    public void throwsAnExceptionIfTheClassPathRepositoryIsNull() {
        File tmpFile = createTmpFile();
        new CodeRepository(null, tmpFile.getParentFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsAnExceptionIfTheRepositoryIsNoDirectory() {
        File tmpFile = createTmpFile();
        new CodeRepository(Collections.<File>emptyList(), tmpFile);
    }

    private File createTmpFile() {
        File tmpFile;
        try {
            tmpFile = createTempFile("JUnit", ".tmp");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tmpFile.deleteOnExit();
        return tmpFile;
    }

}
