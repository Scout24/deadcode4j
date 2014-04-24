package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

/**
 * A <code>Repository</code> represents a directory containing code, resources, configuration, etc.
 * along with an optional file filter.
 *
 * @since 1.2.0
 */
public class Repository {

    private final File directory;
    private final FileFilter fileFilter;

    public Repository(@Nonnull File directory, @Nonnull FileFilter fileFilter) {
        checkArgument(directory.isDirectory(), "No valid directory: " + directory);
        this.directory = directory;
        this.fileFilter = fileFilter;
    }

    public Repository(@Nonnull File directory) {
        this(directory, TRUE);
    }

    @Override
    public String toString() {
        return "Repository @" + this.directory;
    }

    public File getDirectory() {
        return directory;
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }

}
