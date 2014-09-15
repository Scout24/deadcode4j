package de.is24.deadcode4j;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

public class A_DeadCodeComputer {

    private DeadCodeComputer objectUnderTest;
    private Map<String, Set<String>> codeDependencies = newHashMap();

    @Before
    public void setUpObjectUnderTest() {
        this.objectUnderTest = new DeadCodeComputer();
        codeDependencies.clear();
    }

    @Test
    public void recognizesASingleClassAsDeadCode() {
        setUpDependency("SingleClass");

        Collection<String> deadClasses = computeDeadClasses();

        assertThat("Should recognize one class as dead", deadClasses, hasSize(1));
        assertThat(deadClasses, contains("SingleClass"));
    }

    @Test
    public void recognizesTwoInterdependentClassesAsLiveCode() {
        setUpDependency("A", "B");
        setUpDependency("B", "A");

        Collection<String> deadClasses = computeDeadClasses();

        assertThat("Should find NO dead code", deadClasses, hasSize(0));
    }

    @Test
    public void recognizesDependencyChainAsPartlyDeadCode() {
        setUpDependency("DependingClass", "IndependentClass");
        setUpDependency("IndependentClass");

        Collection<String> deadClasses = computeDeadClasses();

        assertThat("Should recognize one class as dead", deadClasses, contains("DependingClass"));
    }

    private void setUpDependency(String depender, String... dependees) {
        codeDependencies.put(depender, newHashSet(dependees));
    }

    private Collection<String> computeDeadClasses() {
        AnalyzedCode analyzedCode = new AnalyzedCode(
                EnumSet.noneOf(AnalysisStage.class), codeDependencies.keySet(), codeDependencies);
        DeadCode deadCode = objectUnderTest.computeDeadCode(analyzedCode);
        return deadCode.getDeadClasses();
    }

}
