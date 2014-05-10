package de.is24.deadcode4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public final class CodeContextBuilder {

    private CodeContextBuilder() { }

    public static CodeContext givenCodeContext(Module module, Map<Object, IntermediateResult> intermediateResults) {
        return new CodeContext(module, intermediateResults);
    }

    public static CodeContext givenCodeContext(Module module) {
        return givenCodeContext(module, Collections.<Object, IntermediateResult>emptyMap());
    }

    public static CodeContext givenCodeContext(Module module, Object key, IntermediateResult intermediateResult) {
        HashMap<Object, IntermediateResult> results = newHashMap();
        results.put(key, intermediateResult);
        return givenCodeContext(module, results);
    }

}
