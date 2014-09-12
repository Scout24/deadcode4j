package de.is24.deadcode4j;

import com.google.common.base.Optional;

import java.io.File;

/**
 * A <code>Resource</code> represents a required artifact/dependency of a <code>Module</code>.
 * It can be a mere class path entry or a <code>Module</code> which is part of the analysis.
 *
 * @since 2.0.0
 */
public abstract class Resource {

    public static Resource of(final File file) {
        return new Resource() {
            @Override
            public Optional<File> getClassPathEntry() {
                return Optional.of(file);
            }

            @Override
            public Optional<Module> getReferencedModule() {
                return Optional.absent();
            }
        };
    }

    public static Resource of(final Module module) {
        return new Resource() {
            @Override
            public Optional<File> getClassPathEntry() {
                Repository repository = module.getOutputRepository();
                return repository != null ? Optional.of(repository.getDirectory()) : Optional.<File>absent();
            }

            @Override
            public Optional<Module> getReferencedModule() {
                return Optional.of(module);
            }
        };
    }

    public abstract Optional<File> getClassPathEntry();

    public abstract Optional<Module> getReferencedModule();

}
