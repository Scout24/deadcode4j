package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;

import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

/**
 * A <code>CodeRepository</code> represents a directory containing code along with an optional file filter.
 *
 * @since 1.2.0
 */
public class CodeRepository {

    private final File directory;
    private final FileFilter fileFilter;

    public CodeRepository(@Nonnull File directory, @Nonnull FileFilter fileFilter) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("No valid directory: " + directory);
        }
        this.directory = directory;
        this.fileFilter = fileFilter;
    }

    public CodeRepository(@Nonnull File directory) {
        this(directory, TRUE);
    }

    @Override
    public String toString() {
        return "CodeRepository @" + this.directory;
    }

    public File getDirectory() {
        return directory;
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }

}
