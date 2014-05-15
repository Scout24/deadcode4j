package de.is24.deadcode4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
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
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Nonnull
    private final Map<Module, Map<Object, IntermediateResult>> intermediateResults = newHashMap();

    public IntermediateResults() {
    }

    /**
     * Returns an <code>IntermediateResultMap</code> for the given <code>Map</code>.<br/>
     * This method is defined for type inference, as it could simply be replaced with a constructor call.
     *
     * @since 1.6
     */
    @Nonnull
    public static <K, V> IntermediateResultMap<K, V> resultMapFor(@Nonnull Map<K, V> intermediateResults) {
        return new IntermediateResultMap<K, V>(intermediateResults);
    }

    /**
     * Returns an <code>IntermediateResultMap</code> from the given <code>CodeContext</code> for the given key.<br/>
     * This method is defined to handle the <i>unchecked</i> cast to a typed <code>IntermediateResultMap</code>,
     * it could simply be replaced with {@link de.is24.deadcode4j.CodeContext#getIntermediateResult(Object)}.
     *
     * @since 1.6
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <K, V> IntermediateResultMap<K, V> resultMapFrom(@Nonnull CodeContext codeContext, Object key) {
        return (IntermediateResultMap<K, V>) codeContext.getIntermediateResult(key);
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

    @Nonnull
    private Map<Object, IntermediateResult> getIntermediateResultsOf(@Nonnull CodeContext codeContext) {
        Map<Object, IntermediateResult> intermediateResults = newHashMap();
        for (Map.Entry<Object, Object> cachedEntry : codeContext.getCache().entrySet()) {
            Object cachedValue = cachedEntry.getValue();
            if (IntermediateResult.class.isInstance(cachedValue)) {
                logger.debug("{} stored [{}].", codeContext.getModule(), cachedValue);
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

    @Nonnull
    private Map<Object, IntermediateResult> calculateResultsOfParentsFor(@Nonnull Module module) {
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

    private void mergeWithResultsOf(@Nonnull Module module, @Nonnull Map<Object, IntermediateResult> combinedResults) {
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

    /**
     * An <code>IntermediateResultMap</code> is an implementation of {@link de.is24.deadcode4j.IntermediateResult} using
     * a <code>Map</code> to store the results. Concerning merging with siblings & parents, it
     * <ul>
     * <li>adds entries of siblings & parents to the results if they don't collide with those defined by itself</li>
     * <li>if an entry of sibling or parent collides with one defined by itself
     * <ul>
     * <li>and the values are <code>Collection</code>s, they are added to one another</li>
     * <li>otherwise, the own value is kept and the other is discarded</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @since 1.6
     */
    public static class IntermediateResultMap<K, V> implements IntermediateResult {
        @Nonnull
        private final Logger logger = LoggerFactory.getLogger(getClass());
        @Nonnull
        private final Map<K, V> results;


        /**
         * Creates an <code>IntermediateResultMap</code> to store the given <code>Map</code>.
         *
         * @since 1.6
         */
        public IntermediateResultMap(@Nonnull Map<K, V> results) {
            this.results = newHashMap(results);
        }

        @Nonnull
        @Override
        public String toString() {
            return getClass().getSimpleName() + ": " + this.results;
        }

        @Nonnull
        @Override
        public IntermediateResult mergeSibling(@Nonnull IntermediateResult sibling) {
            return merge(sibling);
        }

        @Nonnull
        @Override
        public IntermediateResult mergeParent(@Nonnull IntermediateResult parent) {
            return merge(parent);
        }

        /**
         * Returns the stored result <code>Map</code>.
         *
         * @since 1.6
         */
        @Nonnull
        public Map<K, V> getMap() {
            return this.results;
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        private IntermediateResult merge(@Nonnull IntermediateResult result) {
            Map<K, V> mergedResults = newHashMap(getMap());
            for (Map.Entry<K, V> resultEntry : getResults(result).entrySet()) {
                K key = resultEntry.getKey();
                V value = resultEntry.getValue();
                V existingResult = mergedResults.get(key);
                if (existingResult == null) {
                    mergedResults.put(key, value);
                } else if (Collection.class.isInstance(existingResult)) {
                    Collection.class.cast(existingResult).addAll(Collection.class.cast(value));
                } else if (!existingResult.equals(value)) {
                    logger.debug("Intermediate result [{}] refers to [{}] and [{}] defined by different modules, keeping the former.", key, existingResult, value);
                }
            }
            return new IntermediateResultMap<K, V>(mergedResults);
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        private Map<K, V> getResults(IntermediateResult result) {
            return IntermediateResultMap.class.cast(result).getMap();
        }

    }

}
