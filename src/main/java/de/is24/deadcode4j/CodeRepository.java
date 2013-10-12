package de.is24.deadcode4j;

import org.apache.commons.io.filefilter.IOFileFilter;

import javax.annotation.Nonnull;
import java.io.File;

import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

/**
 * A <code>CodeRepository</code> represents a directory containing code along with an optional file filter.
 *
 * @since 1.2.0
 */
public class CodeRepository {

    private final File directory;
    private final IOFileFilter fileFilter;

    public CodeRepository(@Nonnull File directory, @Nonnull IOFileFilter fileFilter) {
        this.fileFilter = fileFilter;
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("No valid directory: " + directory);
        }
        this.directory = directory;
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

    public IOFileFilter getFileFilter() {
        return fileFilter;
    }

}
