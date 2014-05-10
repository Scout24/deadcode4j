package de.is24.deadcode4j;

import de.is24.deadcode4j.junit.LoggingRule;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.IntermediateResults.IntermediateResultMap;
import static org.hamcrest.Matchers.*;

public final class An_IntermediateResultMap {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();

    @Test
    public void createsCopyOfProvidedMap() {
        Map<String, String> results = newHashMap();
        results.put("foo", "bar");
        IntermediateResultMap<String, String> objectUnderTest = createIntermediateResultMap(results);

        results.clear();

        MatcherAssert.assertThat(objectUnderTest.getMap(), hasEntry("foo", "bar"));
    }

    @Test
    public void addsSiblingEntriesIfTheyDoNotCollide() {
        IntermediateResultMap<String, String> objectUnderTest = createIntermediateResultMap("foo", "bar");

        IntermediateResultMap<String, String> mergedResult =
                mergeSibling(objectUnderTest, createIntermediateResultMap("bar", "foo"));

        MatcherAssert.assertThat(mergedResult.getMap(), hasEntry("foo", "bar"));
        MatcherAssert.assertThat(mergedResult.getMap(), hasEntry("bar", "foo"));
    }

    @Test
    public void addsParentEntriesIfTheyDoNotCollide() {
        IntermediateResultMap<String, String> objectUnderTest = createIntermediateResultMap("foo", "bar");

        IntermediateResultMap<String, String> mergedResult =
                mergeParent(objectUnderTest, createIntermediateResultMap("bar", "foo"));

        MatcherAssert.assertThat(mergedResult.getMap(), hasEntry("foo", "bar"));
        MatcherAssert.assertThat(mergedResult.getMap(), hasEntry("bar", "foo"));
    }

    @Test
    public void keepsOwnIfTheyCollideWithSiblingEntries() {
        IntermediateResultMap<String, String> objectUnderTest = createIntermediateResultMap("foo", "bar");

        IntermediateResultMap<String, String> mergedResult =
                mergeSibling(objectUnderTest, createIntermediateResultMap("foo", "foo"));

        MatcherAssert.assertThat(mergedResult.getMap(), hasEntry("foo", "bar"));
    }

    @Test
    public void keepsOwnIfTheyCollideWithParentEntries() {
        IntermediateResultMap<String, String> objectUnderTest = createIntermediateResultMap("foo", "bar");

        IntermediateResultMap<String, String> mergedResult =
                mergeParent(objectUnderTest, createIntermediateResultMap("foo", "foo"));

        MatcherAssert.assertThat(mergedResult.getMap(), hasEntry("foo", "bar"));
    }

    @Test
    public void addsCollidingSiblingEntriesIfValueIsCollection() {
        IntermediateResultMap<String, ArrayList<String>> objectUnderTest =
                createIntermediateResultMap("foo", newArrayList("bar"));

        IntermediateResultMap<String, ArrayList<String>> mergedResult =
                mergeSibling(objectUnderTest, createIntermediateResultMap("foo", newArrayList("foo")));

        MatcherAssert.assertThat(mergedResult.getMap(), hasEntry(is("foo"), contains("bar", "foo")));
    }

    @Test
    public void addsCollidingParentEntriesIfValueIsCollection() {
        IntermediateResultMap<String, HashSet<String>> objectUnderTest =
                createIntermediateResultMap("foo", newHashSet("bar"));

        IntermediateResultMap<String, HashSet<String>> mergedResult =
                mergeParent(objectUnderTest, createIntermediateResultMap("foo", newHashSet("foo")));

        MatcherAssert.assertThat(mergedResult.getMap(), hasEntry(is("foo"), containsInAnyOrder("bar", "foo")));
    }

    private <K, V> IntermediateResultMap<K, V> createIntermediateResultMap(Map<K, V> results) {
        return new IntermediateResultMap<K, V>(results);
    }

    private <K, V> IntermediateResultMap<K, V> createIntermediateResultMap(K key, V value) {
        Map<K, V> results = newHashMap();
        results.put(key, value);
        return new IntermediateResultMap<K, V>(results);
    }

    @SuppressWarnings("unchecked")
    private <K, V> IntermediateResultMap<K, V> mergeSibling(IntermediateResultMap<K, V> objectUnderTest, IntermediateResultMap<K, V> intermediateResultMap) {
        return (IntermediateResultMap<K, V>) objectUnderTest.mergeSibling(intermediateResultMap);
    }

    @SuppressWarnings("unchecked")
    private <K, V> IntermediateResultMap<K, V> mergeParent(IntermediateResultMap<K, V> objectUnderTest, IntermediateResultMap<K, V> intermediateResultMap) {
        return (IntermediateResultMap<K, V>) objectUnderTest.mergeParent(intermediateResultMap);
    }

}