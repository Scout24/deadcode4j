package de.is24.deadcode4j;

import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.IntermediateResults.IntermediateResultSet;
import static de.is24.deadcode4j.IntermediateResults.resultSetFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

public final class An_IntermediateResultSet {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();

    @SuppressWarnings("unchecked")
    private static <E> IntermediateResultSet<E> mergeSibling(IntermediateResultSet<E> objectUnderTest, IntermediateResultSet<E> IntermediateResultSet) {
        return (IntermediateResultSet<E>) objectUnderTest.mergeSibling(IntermediateResultSet);
    }

    @SuppressWarnings("unchecked")
    private static <E> IntermediateResultSet<E> mergeParent(IntermediateResultSet<E> objectUnderTest, IntermediateResultSet<E> IntermediateResultSet) {
        return (IntermediateResultSet<E>) objectUnderTest.mergeParent(IntermediateResultSet);
    }

    @Test
    public void createsCopyOfProvidedSet() {
        Set<String> results = newHashSet("foo");
        IntermediateResultSet<String> objectUnderTest = resultSetFor(results);

        results.clear();

        assertThat(objectUnderTest.getResults(), hasItem("foo"));
    }

    @Test
    public void addsSiblingEntries() {
        IntermediateResultSet<String> objectUnderTest = givenIntermediateResultSet("foo");

        IntermediateResultSet<String> mergedResult =
                mergeSibling(objectUnderTest, givenIntermediateResultSet("bar"));

        assertThat(mergedResult.getResults(), hasItems("foo", "bar"));
    }

    @Test
    public void addsParentEntries() {
        IntermediateResultSet<String> objectUnderTest = givenIntermediateResultSet("foo");

        IntermediateResultSet<String> mergedResult =
                mergeParent(objectUnderTest, givenIntermediateResultSet("bar"));

        assertThat(mergedResult.getResults(), hasItems("foo", "bar"));
    }

    private IntermediateResultSet<String> givenIntermediateResultSet(String element) {
        return resultSetFor(newHashSet(element));
    }

}