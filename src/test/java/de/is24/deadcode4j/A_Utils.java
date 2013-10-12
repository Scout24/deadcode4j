package de.is24.deadcode4j;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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

}
