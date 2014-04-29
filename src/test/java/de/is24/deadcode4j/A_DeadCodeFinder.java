package de.is24.deadcode4j;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

public final class A_DeadCodeFinder {

    private DeadCodeFinder deadCodeFinder;
    private Map<String, Set<String>> codeDependencies = newHashMap();

    @Before
    public void setUpObjectUnderTest() {
        Set<Analyzer> analyzers = emptySet();
        this.deadCodeFinder = new DeadCodeFinder(analyzers);
        codeDependencies.clear();
    }

    @Test
    public void recognizesASingleClassAsDeadCode() {
        setUpDependency("SingleClass");
        Collection<String> deadCode = deadCodeFinder.determineDeadClasses(provideAnalyzedCode());

        assertThat("Should recognize one class as dead", deadCode, hasSize(1));
        assertThat(deadCode, contains("SingleClass"));
    }

    @Test
    public void recognizesTwoInterdependentClassesAsLiveCode() {
        setUpDependency("A", "B");
        setUpDependency("B", "A");
        Collection<String> deadCode = deadCodeFinder.determineDeadClasses(provideAnalyzedCode());

        assertThat("Should find NO dead code", deadCode, hasSize(0));
    }

    @Test
    public void recognizesDependencyChainAsPartlyDeadCode() {
        setUpDependency("DependingClass", "IndependentClass");
        setUpDependency("IndependentClass");
        Collection<String> deadCode = deadCodeFinder.determineDeadClasses(provideAnalyzedCode());

        assertThat("Should recognize one class as dead", deadCode, hasSize(1));
    }

    private void setUpDependency(String depender, String... dependees) {
        codeDependencies.put(depender, newHashSet(dependees));
    }

    private AnalyzedCode provideAnalyzedCode() {
        return new AnalyzedCode(codeDependencies.keySet(), codeDependencies);
    }

}
