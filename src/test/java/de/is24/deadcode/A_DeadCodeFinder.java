package de.is24.deadcode;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public final class A_DeadCodeFinder {

    private DeadCodeFinder deadCodeFinder;

    @Before
    public void setUpObjectUnderTest() {
        this.deadCodeFinder = new DeadCodeFinder();
    }

    @Test
    public void recognizesASingleClassAsDeadCode() {
        DeadCode deadCode = deadCodeFinder.findDeadCode(getTestSetup("singleclass"));

        assertThat("Should analyze one class", deadCode.getAnalyzedClasses(),hasSize(1));
        assertThat("Should recognize one class as dead", deadCode.getDeadClasses(), hasSize(1));
    }

    @Test
    public void recognizesTwoInterdependentClassesAsLiveCode() {
        DeadCode deadCode = deadCodeFinder.findDeadCode(getTestSetup("interdependentclasses"));

        assertThat("Should analyze two classes", deadCode.getAnalyzedClasses(), hasSize(2));
        assertThat("Should find NO dead code", deadCode.getDeadClasses(), hasSize(0));
    }

    @Test
    public void recognizesDependencyChainAsPartlyDeadCode() {
        DeadCode deadCode = deadCodeFinder.findDeadCode(getTestSetup("dependencychain"));

        assertThat("Should analyze two classes", deadCode.getAnalyzedClasses(), hasSize(2));
        assertThat("Should recognize one class as dead", deadCode.getDeadClasses(), hasSize(1));
    }

    @Test
    public void recognizesTwoInterdependentClassesFromDifferentSourcesAsLiveCode() {
        DeadCode deadCode = deadCodeFinder.findDeadCode(getTestSetup("source1"), getTestSetup("source2"));

        assertThat("Should analyze two classes", deadCode.getAnalyzedClasses(), hasSize(2));
        assertThat("Should find NO dead code", deadCode.getDeadClasses(), hasSize(0));
    }

    private File getTestSetup(String scenario) {
        return new File(System.getProperty("java.io.tmpdir") + "/" + scenario);
    }

}
