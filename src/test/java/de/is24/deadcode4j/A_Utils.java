package de.is24.deadcode4j;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_Utils {

    @Test
    public void doesNotAddNull() {
        ArrayList<Object> list = newArrayList();

        boolean changed = Utils.addIfNonNull(list, null);

        assertThat("The collection shouldn't have changed!", changed, is(false));
        assertThat(list, hasSize(0));
    }

    @Test
    public void addsNotNullValue() {
        ArrayList<Object> list = newArrayList();

        boolean changed = Utils.addIfNonNull(list, new Object());

        assertThat("The collection should have changed!", changed, is(true));
        assertThat(list, hasSize(1));
    }

    @Test
    public void returnsDefaultValue() {
        final int defaultValue = 42;
        Map<String, Integer> map = newHashMap();
        map.put("key", 23);

        Integer value = Utils.getValueOrDefault(map, "anotherKey", defaultValue);

        assertThat(value, is(defaultValue));
    }

    @Test
    public void returnsMappedValue() {
        final int mappedValue = 42;
        Map<String, Integer> map = newHashMap();
        map.put("key", mappedValue);

        Integer value = Utils.getValueOrDefault(map, "key", 23);

        assertThat(value, is(mappedValue));
    }

    @Test
    public void addsSetToMapIfNoMappedSetExists() {
        final String key = "foo";
        final Integer value = 42;
        Map<String, Set<Integer>> map = newHashMap();

        Utils.addToMappedSet(map, key, value);

        Set<Integer> values = newHashSet(value);
        assertThat(map, hasEntry(key, values));
    }

    @Test
    public void preservesExistingSetIfMappedSetExists() {
        final String key = "foo";
        final Integer newValue = 42;
        Map<String, Set<Integer>> map = newHashMap();
        Set<Integer> existingSet = newHashSet();
        map.put(key, existingSet);

        Utils.addToMappedSet(map, key, newValue);

        assertThat(map, hasEntry(is(equalTo(key)), is(sameInstance(existingSet))));
    }

}
