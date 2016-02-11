package de.is24.deadcode4j;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.is24.deadcode4j.ModuleBuilder.givenModule;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public final class A_Module {

    @Test
    public void sortsModulesByDependency() {
        Module c = givenModule("C");
        Module b = givenModule("B", c);
        Module a = givenModule("A", b);

        Iterable<Module> modules = Module.sort(asList(b, a, c));

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

    @Test(expected = RuntimeException.class)
    public void throwsExceptionIfSortingFails() {
        ArrayList<Resource> dependencies = new ArrayList<Resource>();
        Module a = givenModule("A", null, dependencies);
        Module b = givenModule("B", a);
        dependencies.add(Resource.of(b));

        Module.sort(asList(a, b));
    }

}