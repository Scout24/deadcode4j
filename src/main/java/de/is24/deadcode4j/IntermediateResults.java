package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Instances of <code>IntermediateResults</code> are used to keep track of and calculate the {@link IntermediateResult}s
 * produced by and being made available to {@link CodeContext} instances, respectively.
 *
 * @since 1.6
 */
public final class IntermediateResults {

    @Nonnull
    private final Map<Module, Map<Object, IntermediateResult>> intermediateResults = newHashMap();

    public IntermediateResults() {
    }

    /**
     * Adds the intermediate results of the given code context's cache.
     *
     * @since 1.6
     */
    public void add(@Nonnull CodeContext codeContext) {
        intermediateResults.put(codeContext.getModule(), getIntermediateResultsOf(codeContext));
    }

    /**
     * Calculates the intermediate results being available for the specified module.
     *
     * @since 1.6
     */
    @Nonnull
    public Map<Object, IntermediateResult> calculateIntermediateResultsFor(@Nonnull Module module) {
        return calculateIntermediateResults(module);
    }

    private Map<Object, IntermediateResult> getIntermediateResultsOf(CodeContext codeContext) {
        Map<Object, IntermediateResult> intermediateResults = newHashMap();
        for (Map.Entry<Object, Object> cachedEntry : codeContext.getCache().entrySet()) {
            Object cachedValue = cachedEntry.getValue();
            if (IntermediateResult.class.isInstance(cachedValue)) {
                intermediateResults.put(cachedEntry.getKey(), IntermediateResult.class.cast(cachedValue));
            }
        }
        return intermediateResults;
    }

    @Nonnull
    private Map<Object, IntermediateResult> calculateIntermediateResults(@Nonnull Module module) {
        Map<Object, IntermediateResult> results = calculateResultsOfParentsFor(module);
        mergeWithResultsOf(module, results);
        return results;
    }

    private Map<Object, IntermediateResult> calculateResultsOfParentsFor(Module module) {
        Map<Object, IntermediateResult> mergedResults = newHashMap();
        for (Module requiredModule : module.getRequiredModules()) {
            Map<Object, IntermediateResult> results = calculateIntermediateResults(requiredModule);
            for (Map.Entry<Object, IntermediateResult> resultEntry : results.entrySet()) {
                Object key = resultEntry.getKey();
                IntermediateResult intermediateResult = resultEntry.getValue();
                IntermediateResult existingResult = mergedResults.get(key);
                mergedResults.put(key, existingResult == null
                                ? intermediateResult
                                : existingResult.mergeSibling(intermediateResult)
                );
            }
        }
        return mergedResults;
    }

    private void mergeWithResultsOf(Module module, Map<Object, IntermediateResult> combinedResults) {
        Map<Object, IntermediateResult> intermediateResultsOfModule = intermediateResults.get(module);
        if (intermediateResultsOfModule == null) {
            return;
        }
        for (Map.Entry<Object, IntermediateResult> resultEntry : intermediateResultsOfModule.entrySet()) {
            Object key = resultEntry.getKey();
            IntermediateResult intermediateResult = resultEntry.getValue();
            IntermediateResult parentResult = combinedResults.get(key);
            combinedResults.put(key, parentResult == null
                            ? intermediateResult
                            : intermediateResult.mergeParent(parentResult)
            );
        }
    }

}
