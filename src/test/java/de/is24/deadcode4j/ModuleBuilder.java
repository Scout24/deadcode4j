package de.is24.deadcode4j;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;

public final class ModuleBuilder {
    private ModuleBuilder() {
    }

    public static Module givenModule(String moduleId, Module... dependencies) {
        return new Module(moduleId, "UTF-8", transform(asList(dependencies), toResource()), null, Collections.<Repository>emptyList());
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
