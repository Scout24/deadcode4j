package de.is24.deadcode4j;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

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
        Map<String, Set<Integer>> map = newHashMap();

        Set<Integer> set = Utils.getOrAddMappedSet(map, key);

        assertThat(map, hasEntry(is(equalTo(key)), is(sameInstance(set))));
    }

    @Test
    public void preservesExistingSetIfMappedSetExists() {
        final String key = "foo";
        Map<String, Set<Integer>> map = newHashMap();
        Set<Integer> existingSet = newHashSet();
        map.put(key, existingSet);

        Set<Integer> set = Utils.getOrAddMappedSet(map, key);

        assertThat(set, is(sameInstance(existingSet)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void callsEachFunction() {
        Object expectedReturnValue = new Object();
        Function<Object, Optional<Object>> first = mock(Function.class, "firstFunction");
        when(first.apply(anyObject())).thenReturn(absent());
        Function<Object, Optional<Object>> second = mock(Function.class, "secondFunction");
        when(second.apply(anyObject())).thenReturn(of(expectedReturnValue));

        Optional<Object> result = Utils.or(first, second).apply(new Object());

        assert result != null;
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(expectedReturnValue));
        verify(first).apply(anyObject());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void stopsAfterResultIsPresent() {
        Object expectedReturnValue = new Object();
        Function<Object, Optional<Object>> first = mock(Function.class, "firstFunction");
        when(first.apply(anyObject())).thenReturn(of(expectedReturnValue));
        Function<Object, Optional<Object>> second = mock(Function.class, "secondFunction");
        when(second.apply(anyObject())).thenReturn(absent());

        Optional<Object> result = Utils.or(first, second).apply(new Object());

        assert result != null;
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(expectedReturnValue));
        verify(second, never()).apply(anyObject());
    }

}
