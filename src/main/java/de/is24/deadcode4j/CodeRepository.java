package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * A <code>CodeRepository</code> represents a directory containing code along with an optional file filter.
 *
 * @since 1.2.0
 */
public class CodeRepository {

    private final File directory;

    public CodeRepository(@Nonnull File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("No valid directory: " + directory);
        }
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }

    @Override
    public String toString() {
        return "CodeRepository @" + this.directory;
    }

}
