package de.is24.deadcode4j.junit;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;
import java.io.IOException;

import static java.io.File.createTempFile;

public class TempFileRule extends TestWatcher {

    private File tempFile;

    @Override
    protected void starting(Description description) {
        try {
            tempFile = createTempFile("JUnit", ".tmp");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file!", e);
        }
        tempFile.deleteOnExit();
    }


    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void finished(Description description) {
        if (tempFile != null) {
            tempFile.delete();
        }
    }

    public File getTempFile() {
        return tempFile;
    }

}
