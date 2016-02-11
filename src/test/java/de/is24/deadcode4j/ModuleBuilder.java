package de.is24.deadcode4j;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;

public final class ModuleBuilder {
    private ModuleBuilder() {
    }

    public static Module givenModule(String moduleId, File repository, Collection<Resource> dependencies) {
        return new Module(
                moduleId,
                "UTF-8",
                dependencies,
                repository == null ? null : new Repository(repository),
                Collections.<Repository>emptyList());
    }

    public static Module givenModule(String moduleId, File repository, Module... dependencies) {
        return givenModule(moduleId, repository, transform(asList(dependencies), toResource()));
    }

    public static Module givenModule(String moduleId, Module... dependencies) {
        return givenModule(moduleId, null, dependencies);
    }

    public static Module givenModule(String moduleId) {
        return givenModule(moduleId, new Module[0]);
    }

    private static Function<? super Module, Resource> toResource() {
        return new Function<Module, Resource>() {
            @Nullable
            @Override
            public Resource apply(@Nullable Module input) {
                return input == null ? null : Resource.of(input);
            }
        };
    }

}
