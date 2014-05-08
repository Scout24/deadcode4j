package de.is24.deadcode4j;

import com.google.common.base.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static de.is24.deadcode4j.Utils.addIfNonNull;

/**
 * A <code>Module</code> represents a java module from the deadcode4j perspective.
 *
 * @since 1.6
 */
public class Module {

    @Nonnull
    private final String moduleId;
    @Nullable
    private final String encoding;
    @Nonnull
    private final Collection<Resource> dependencies;
    @Nullable
    private final Repository outputRepository;
    @Nonnull
    private final List<Repository> allRepositories;

    /**
     * Creates a new <code>Module</code>.
     *
     * @param moduleId         the module's identifier
     * @param encoding         the module's source encoding
     * @param dependencies     the resources this module depends on
     * @param outputRepository the "output" repository - i.e. the directory where compiled classes can be found
     * @param repositories     additional repositories to analyze
     * @since 1.6
     */
    public Module(@Nonnull String moduleId,
                  @Nullable String encoding,
                  @Nonnull Collection<Resource> dependencies,
                  @Nullable Repository outputRepository,
                  @Nonnull Iterable<Repository> repositories) {
        this.moduleId = moduleId;
        this.encoding = encoding;
        this.dependencies = dependencies;
        this.outputRepository = outputRepository;
        this.allRepositories = newArrayList();
        addIfNonNull(allRepositories, outputRepository);
        addAll(allRepositories, repositories);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || Module.class.isInstance(obj) && this.moduleId.equals(Module.class.cast(obj).moduleId);
    }

    @Override
    public int hashCode() {
        return this.moduleId.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder buffy = new StringBuilder("Module [").append(this.moduleId).append("] with");
        if (this.outputRepository == null) {
            buffy.append("out");
        }
        buffy.append(" output repository");
        if (this.allRepositories.size() > 1) {
            buffy.append(" and ").append(this.allRepositories.size() - 1).append(" additional repositories");
        }
        return buffy.toString();
    }

    /**
     * Returns the module's ID.
     *
     * @since 1.6
     */
    @Nonnull
    public String getModuleId() {
        return moduleId;
    }


    /**
     * Returns the module's source file encoding.
     *
     * @since 1.6
     */
    @Nullable
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns all class path entries of this module.
     *
     * @since 1.6
     */
    @Nonnull
    public Iterable<File> getClassPath() {
        List<File> classPath = newArrayListWithCapacity(dependencies.size());
        for (Resource dependency : dependencies) {
            Optional<File> classPathEntry = dependency.getClassPathEntry();
            if (classPathEntry.isPresent()) {
                classPath.add(classPathEntry.get());
            }
        }
        return classPath;
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

    private boolean requires(@Nonnull Module module) {
        for (Resource dependency : dependencies) {
            Optional<Module> optionalModule = dependency.getReferencedModule();
            if (!optionalModule.isPresent()) {
                continue;
            }
            Module referencedModule = optionalModule.get();
            if (module.equals(referencedModule) || referencedModule.requires(module)) {
                return true;
            }
        }
        return false;
    }

}
