package de.is24.deadcode4j;

import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

/**
 * A <code>CodeRepository</code> represents a directory containing code along with an optional file filter.
 *
 * @since 1.2.0
 */
public class CodeRepository {

    private final File directory;
    private final FileFilter fileFilter;
    private final Iterable<File> classPath;

    public CodeRepository(@Nonnull Iterable<File> classPath, @Nonnull File directory, @Nonnull FileFilter fileFilter) {
        checkNotNull(classPath, "ClassPath cannot be null!");
        checkArgument(directory.isDirectory(), "No valid directory: " + directory);
        this.classPath = Sets.newHashSet(classPath);
        this.directory = directory;
        this.fileFilter = fileFilter;
    }

    public CodeRepository(@Nonnull Iterable<File> classPath, @Nonnull File directory) {
        this(classPath, directory, TRUE);
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

    public Iterable<File> getClassPath() {
        return classPath;
    }

}
