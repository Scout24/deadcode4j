package de.is24.deadcode4j;

import com.google.common.base.Function;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public final class A_Module {

    @Test
    public void sortsModulesByDependency() {
        Module c = givenModule("C");
        Module b = givenModule("B", c);
        Module a = givenModule("A", b);

        Iterable<Module> modules = Module.sort(asList(b,a, c));

        assertThat(modules, contains(c, b, a));
    }

    @Test
    public void sortsUnrelatedModulesAlphabetically() {
        Module a = givenModule("A");
        Module b = givenModule("B");
        Module c = givenModule("C");

        Iterable<Module> modules = Module.sort(asList(c, b, a));

        assertThat(modules, contains(a, b, c));
    }

    @Test
    public void sortsReallyComplexDependencyGraphCorrectly() {
        Module z = givenModule("Z");
        Module y = givenModule("Y", z);
        Module c = givenModule("C", z);
        Module b = givenModule("B", c, y);
        Module a = givenModule("A", b);
        Module x = givenModule("X", c);

        List<Module> unsortedModules = asList(a, b, c, x, y, z);
        Collections.shuffle(unsortedModules);
        Iterable<Module> modules = Module.sort(unsortedModules);

        assertThat(modules, contains(z, c, y, b, x, a));
    }

    private Module givenModule(String moduleId, Module... dependencies) {
        return new Module(moduleId, "UTF-8", transform(asList(dependencies), toResource()), null, Collections.<Repository>emptyList());
    }

    private Module givenModule(String moduleId) {
        return givenModule(moduleId, new Module[0]);
    }

    private Function<? super Module, Resource> toResource() {
        return new Function<Module, Resource>() {
            @Nullable
            @Override
            public Resource apply(@Nullable Module input) {
                return input == null ? null : Resource.of(input);
            }
        };
    }

}