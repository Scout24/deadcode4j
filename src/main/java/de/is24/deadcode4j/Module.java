package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;
import static de.is24.deadcode4j.Utils.addIfNonNull;

/**
 * A <code>Module</code> represents a java module from the deadcode4j perspective.
 *
 * @since 1.6
 */
public class Module {

    @Nullable
    private final Repository outputRepository;
    @Nonnull
    private final List<File> classPath;
    @Nonnull
    private final List<Repository> allRepositories;

    /**
     * Creates a new <code>Module</code>.
     *
     * @param outputRepository the "output" repository - i.e. the directory where compiled classes can be found
     * @param classPath        the class path entries of this module
     * @param repositories     additional repositories to analyze
     * @since 1.6
     */
    public Module(@Nullable Repository outputRepository, @Nonnull Iterable<File> classPath, @Nonnull Iterable<Repository> repositories) {
        this.outputRepository = outputRepository;
        this.classPath = newArrayList(classPath);
        this.allRepositories = newArrayList();
        addIfNonNull(allRepositories, outputRepository);
        addAll(allRepositories, repositories);
    }

    @Override
    public String toString() {
        StringBuilder buffy = new StringBuilder("Module ");
        if (this.outputRepository == null) {
            buffy.append("without output repository");
        } else {
            buffy.append("@").append(this.outputRepository);
        }
        if (this.allRepositories.size() > 1) {
            buffy.append(" and ").append(this.allRepositories.size() - 1).append(" additional repositories");
        }
        return buffy.toString();
    }

    /**
     * Returns the "output" repository - i.e. the directory where compiled classes can be found.
     *
     * @since 1.6
     */
    @Nullable
    public Repository getOutputRepository() {
        return outputRepository;
    }

    /**
     * Returns all repositories to analyze (including the {@link #getOutputRepository() output repository}).
     *
     * @since 1.6
     */
    @Nonnull
    public Iterable<Repository> getAllRepositories() {
        return this.allRepositories;
    }


    /**
     * Returns all class path entries of this module.
     *
     * @since 1.6
     */
    @Nonnull
    public Iterable<File> getClassPath() {
        return classPath;
    }

}
