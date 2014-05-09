package de.is24.deadcode4j;

import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static de.is24.deadcode4j.ModuleBuilder.givenModule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class An_IntermediateResults {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();
    private IntermediateResults objectUnderTest;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new IntermediateResults();
    }

    @Test
    public void transfersIntermediateResultsForDependingModule() {
        Object key = getClass();
        CodeContext parentContext = new CodeContext(givenModule("A"));
        parentContext.getCache().put(key, new AnIntermediateResult("ForA"));

        objectUnderTest.add(parentContext);
        Map<Object, IntermediateResult> intermediateResults =
                objectUnderTest.calculateIntermediateResultsFor(givenModule("B", parentContext.getModule()));

        assertThat(intermediateResults, hasEntry(is(key), hasToString("ForA")));
    }

    @Test
    public void transfersIntermediateResultsForDependingModuleOfDependingModule() {
        Object key = getClass();
        CodeContext parentContext = new CodeContext(givenModule("A"));
        parentContext.getCache().put(key, new AnIntermediateResult("ForA"));

        objectUnderTest.add(parentContext);
        Map<Object, IntermediateResult> intermediateResults = objectUnderTest.calculateIntermediateResultsFor(
                givenModule("C", givenModule("B", parentContext.getModule())));

        assertThat(intermediateResults, hasEntry(is(key), hasToString("ForA")));
    }

    @Test
    public void mergesIntermediateResultsOnSiblingLevel() {
        Object key = getClass();
        CodeContext parentContext = new CodeContext(givenModule("A"));
        parentContext.getCache().put(key, new AnIntermediateResult("ForA"));
        CodeContext secondParentContext = new CodeContext(givenModule("B"));
        secondParentContext.getCache().put(key, new AnIntermediateResult("ForB"));

        objectUnderTest.add(parentContext);
        objectUnderTest.add(secondParentContext);
        Map<Object, IntermediateResult> intermediateResults = objectUnderTest.calculateIntermediateResultsFor(
                givenModule("C", parentContext.getModule(), secondParentContext.getModule()));

        assertThat(intermediateResults, hasEntry(is(key), hasToString("ForA+ForB")));
    }

    @Test
    public void mergesIntermediateResultsOnParentLevel() {
        Object key = getClass();
        CodeContext rootContext = new CodeContext(givenModule("A"));
        rootContext.getCache().put(key, new AnIntermediateResult("ForA"));
        CodeContext parentContext = new CodeContext(givenModule("B", rootContext.getModule()));
        parentContext.getCache().put(key, new AnIntermediateResult("ForB"));

        objectUnderTest.add(rootContext);
        objectUnderTest.add(parentContext);
        Map<Object, IntermediateResult> intermediateResults = objectUnderTest.calculateIntermediateResultsFor(
                givenModule("C", parentContext.getModule()));

        assertThat(intermediateResults, hasEntry(is(key), hasToString("ForB->(ForA)")));
    }

    @Test
    public void nowAllTogether() {
        Object key = getClass();
        CodeContext rootContext = new CodeContext(givenModule("A"));
        rootContext.getCache().put(key, new AnIntermediateResult("ForA"));
        CodeContext secondRootContext = new CodeContext(givenModule("Z"));
        secondRootContext.getCache().put(key, new AnIntermediateResult("ForZ"));
        CodeContext parentContext = new CodeContext(givenModule("B", rootContext.getModule()));
        parentContext.getCache().put(key, new AnIntermediateResult("ForB"));
        CodeContext secondParentContext = new CodeContext(givenModule("C", rootContext.getModule(), secondRootContext.getModule()));
        secondParentContext.getCache().put(key, new AnIntermediateResult("ForC"));
        CodeContext thirdParentContext = new CodeContext(givenModule("D"));
        thirdParentContext.getCache().put(key, new AnIntermediateResult("ForD"));
        CodeContext fourthParentContext = new CodeContext(givenModule("Y", secondRootContext.getModule()));
        fourthParentContext.getCache().put(key, new AnIntermediateResult("ForY"));
        CodeContext childContext = new CodeContext(givenModule("X", fourthParentContext.getModule()));
        childContext.getCache().put(key, new AnIntermediateResult("ForX"));

        objectUnderTest.add(rootContext);
        objectUnderTest.add(secondRootContext);
        objectUnderTest.add(parentContext);
        objectUnderTest.add(secondParentContext);
        objectUnderTest.add(thirdParentContext);
        objectUnderTest.add(fourthParentContext);
        objectUnderTest.add(childContext);
        Map<Object, IntermediateResult> intermediateResults = objectUnderTest.calculateIntermediateResultsFor(
                givenModule("E",
                        parentContext.getModule(),
                        secondParentContext.getModule(),
                        thirdParentContext.getModule(),
                        childContext.getModule())
        );

        assertThat(intermediateResults, hasEntry(is(key), hasToString("ForB->(ForA)+ForC->(ForA+ForZ)+ForD+ForX->(ForY->(ForZ))")));
    }

    private static class AnIntermediateResult implements IntermediateResult {
        private final String string;

        public AnIntermediateResult(String string) {
            this.string = string;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || AnIntermediateResult.class.isInstance(obj) && this.string.equals(AnIntermediateResult.class.cast(obj).string);
        }

        @Override
        public int hashCode() {
            return this.string.hashCode();
        }

        @Override
        public String toString() {
            return this.string;
        }

        @Override
        public IntermediateResult mergeSibling(IntermediateResult sibling) {
            return new AnIntermediateResult(this.string + "+" + sibling);
        }

        @Override
        public IntermediateResult mergeParent(IntermediateResult parent) {
            return new AnIntermediateResult(this.string + "->(" + parent + ")");
        }

    }

}