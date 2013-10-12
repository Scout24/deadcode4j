package de.is24.deadcode4j.plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;

/**
 * A <code>SubDirectoryFilter</code> only accepts a specific subdirectory of a directory.
 *
 * @since 1.2.0
 */
public class SubDirectoryFilter implements FileFilter {

    private final File directory;
    private final String subDirectoryName;

    public SubDirectoryFilter(@Nonnull File directory, @Nonnull String subDirectoryName) {
        this.directory = directory;
        this.subDirectoryName = subDirectoryName;
    }

    @Override
    public boolean accept(File file) {
        return file.isDirectory() && directory.equals(file.getParentFile()) && subDirectoryName.equals(file.getName());
    }

}
