package de.is24.deadcode4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public final class AnalysisContextBuilder {

    private AnalysisContextBuilder() { }

    public static AnalysisContext givenAnalysisContext(Module module, Map<Object, IntermediateResult> intermediateResults) {
        return new AnalysisContext(module, intermediateResults);
    }

    public static AnalysisContext givenAnalysisContext(Module module) {
        return givenAnalysisContext(module, Collections.<Object, IntermediateResult>emptyMap());
    }

    public static AnalysisContext givenAnalysisContext(Module module, Object key, IntermediateResult intermediateResult) {
        HashMap<Object, IntermediateResult> results = newHashMap();
        results.put(key, intermediateResult);
        return givenAnalysisContext(module, results);
    }

}
