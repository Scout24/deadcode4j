package de.is24.deadcode4j;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public final class IntermediateResultMapBuilder {

    private IntermediateResultMapBuilder() { }

    public static <K, V> IntermediateResults.IntermediateResultMap<K, V> givenIntermediateResultMap(K key, V value) {
        Map<K, V> results = newHashMap();
        results.put(key, value);
        return new IntermediateResults.IntermediateResultMap<K, V>(results);
    }

}
