package de.is24.deadcode4j;

import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.IntermediateResultMapBuilder.givenIntermediateResultMap;
import static de.is24.deadcode4j.IntermediateResults.IntermediateResultMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class An_IntermediateResultMap {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();

    @SuppressWarnings("unchecked")
    private static <K, V> IntermediateResultMap<K, V> mergeSibling(IntermediateResultMap<K, V> objectUnderTest, IntermediateResultMap<K, V> intermediateResultMap) {
        return (IntermediateResultMap<K, V>) objectUnderTest.mergeSibling(intermediateResultMap);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> IntermediateResultMap<K, V> mergeParent(IntermediateResultMap<K, V> objectUnderTest, IntermediateResultMap<K, V> intermediateResultMap) {
        return (IntermediateResultMap<K, V>) objectUnderTest.mergeParent(intermediateResultMap);
    }

    @Test
    public void createsCopyOfProvidedMap() {
        Map<String, String> results = newHashMap();
        results.put("foo", "bar");
        IntermediateResultMap<String, String> objectUnderTest = IntermediateResults.resultMapFor(results);

        results.clear();

        assertThat(objectUnderTest.getResults(), hasEntry("foo", "bar"));
    }

    @Test
    public void addsSiblingEntriesIfTheyDoNotCollide() {
        IntermediateResultMap<String, String> objectUnderTest = givenIntermediateResultMap("foo", "bar");

        IntermediateResultMap<String, String> mergedResult =
                mergeSibling(objectUnderTest, givenIntermediateResultMap("bar", "foo"));

        assertThat(mergedResult.getResults(), hasEntry("foo", "bar"));
        assertThat(mergedResult.getResults(), hasEntry("bar", "foo"));
    }

    @Test
    public void addsParentEntriesIfTheyDoNotCollide() {
        IntermediateResultMap<String, String> objectUnderTest = givenIntermediateResultMap("foo", "bar");

        IntermediateResultMap<String, String> mergedResult =
                mergeParent(objectUnderTest, givenIntermediateResultMap("bar", "foo"));

        assertThat(mergedResult.getResults(), hasEntry("foo", "bar"));
        assertThat(mergedResult.getResults(), hasEntry("bar", "foo"));
    }

    @Test
    public void keepsOwnIfTheyCollideWithSiblingEntries() {
        IntermediateResultMap<String, String> objectUnderTest = givenIntermediateResultMap("foo", "bar");

        IntermediateResultMap<String, String> mergedResult =
                mergeSibling(objectUnderTest, givenIntermediateResultMap("foo", "foo"));

        assertThat(mergedResult.getResults(), hasEntry("foo", "bar"));
    }

    @Test
    public void keepsOwnIfTheyCollideWithParentEntries() {
        IntermediateResultMap<String, String> objectUnderTest = givenIntermediateResultMap("foo", "bar");

        IntermediateResultMap<String, String> mergedResult =
                mergeParent(objectUnderTest, givenIntermediateResultMap("foo", "foo"));

        assertThat(mergedResult.getResults(), hasEntry("foo", "bar"));
    }

    @Test
    public void addsCollidingSiblingEntriesIfValueIsCollection() {
        IntermediateResultMap<String, ArrayList<String>> objectUnderTest =
                givenIntermediateResultMap("foo", newArrayList("bar"));

        IntermediateResultMap<String, ArrayList<String>> mergedResult =
                mergeSibling(objectUnderTest, givenIntermediateResultMap("foo", newArrayList("foo")));

        assertThat(mergedResult.getResults(), hasEntry(is("foo"), contains("bar", "foo")));
    }

    @Test
    public void addsCollidingParentEntriesIfValueIsCollection() {
        IntermediateResultMap<String, HashSet<String>> objectUnderTest =
                givenIntermediateResultMap("foo", newHashSet("bar"));

        IntermediateResultMap<String, HashSet<String>> mergedResult =
                mergeParent(objectUnderTest, givenIntermediateResultMap("foo", newHashSet("foo")));

        assertThat(mergedResult.getResults(), hasEntry(is("foo"), containsInAnyOrder("bar", "foo")));
    }

}