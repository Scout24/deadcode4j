package de.is24.deadcode4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Instances of <code>IntermediateResults</code> are used to keep track of and calculate the {@link IntermediateResult}s
 * produced by and being made available to {@link AnalysisContext} instances, respectively.
 *
 * @since 2.0.0
 */
public final class IntermediateResults {
    @Nonnull
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Nonnull
    private final Map<Module, Map<Object, IntermediateResult>> intermediateResults = newHashMap();

    /**
     * Returns an <code>IntermediateResultSet</code> for the given <code>Set</code>.<br/>
     * This method is defined for type inference, as it could simply be replaced with a constructor call.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static <E> IntermediateResultSet<E> resultSetFor(@Nonnull Collection<E> intermediateResults) {
        return new IntermediateResultSet<E>(intermediateResults);
    }

    /**
     * Returns an <code>IntermediateResultMap</code> for the given <code>Map</code>.<br/>
     * This method is defined for type inference, as it could simply be replaced with a constructor call.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static <K, V> IntermediateResultMap<K, V> resultMapFor(@Nonnull Map<K, V> intermediateResults) {
        return new IntermediateResultMap<K, V>(intermediateResults);
    }

    /**
     * Returns an <code>IntermediateResultSet</code> from the given <code>AnalysisContext</code> for the given key.<br/>
     * This method is defined to handle the <i>unchecked</i> cast to a typed <code>IntermediateResultSet</code>,
     * it could simply be replaced with {@link AnalysisContext#getIntermediateResult(Object)}.
     *
     * @since 2.0.0
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <E> IntermediateResultSet<E> resultSetFrom(@Nonnull AnalysisContext analysisContext, Object key) {
        return (IntermediateResultSet<E>) analysisContext.getIntermediateResult(key);
    }

    /**
     * Returns an <code>IntermediateResultMap</code> from the given <code>AnalysisContext</code> for the given key.<br/>
     * This method is defined to handle the <i>unchecked</i> cast to a typed <code>IntermediateResultMap</code>,
     * it could simply be replaced with {@link AnalysisContext#getIntermediateResult(Object)}.
     *
     * @since 2.0.0
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <K, V> IntermediateResultMap<K, V> resultMapFrom(@Nonnull AnalysisContext analysisContext, Object key) {
        return (IntermediateResultMap<K, V>) analysisContext.getIntermediateResult(key);
    }

    /**
     * Adds the intermediate results of the given analysis context's cache.
     *
     * @since 2.0.0
     */
    public void add(@Nonnull AnalysisContext analysisContext) {
        intermediateResults.put(analysisContext.getModule(), getIntermediateResultsOf(analysisContext));
    }

    /**
     * Calculates the intermediate results being available for the specified module.
     *
     * @since 2.0.0
     */
    @Nonnull
    public Map<Object, IntermediateResult> calculateIntermediateResultsFor(@Nonnull Module module) {
        return calculateIntermediateResults(module);
    }

    @Nonnull
    private Map<Object, IntermediateResult> getIntermediateResultsOf(@Nonnull AnalysisContext analysisContext) {
        Map<Object, IntermediateResult> intermediateResults = newHashMap();
        for (Map.Entry<Object, Object> cachedEntry : analysisContext.getCache().entrySet()) {
            Object cachedValue = cachedEntry.getValue();
            if (IntermediateResult.class.isInstance(cachedValue)) {
                logger.debug("{} stored [{}].", analysisContext.getModule(), cachedValue);
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
     * An <code>IntermediateResultSet</code> is an implementation of {@link de.is24.deadcode4j.IntermediateResult} using
     * a <code>Set</code> to store the results. Concerning merging with siblings & parents, it simply adds both sets.
     *
     * @since 2.0.0
     */
    public static class IntermediateResultSet<E> implements IntermediateResult {

        @Nonnull
        private final Set<E> results;

        /**
         * Creates an <code>IntermediateResultSet</code> to store the given <code>Set</code>.
         *
         * @since 2.0.0
         */
        public IntermediateResultSet(@Nonnull Collection<E> results) {
            this.results = Collections.unmodifiableSet(newHashSet(results));
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + ": " + results;
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
         * Returns the stored read-only <code>Set</code>.
         *
         * @since 2.0.0
         */
        @Nonnull
        public Set<E> getResults() {
            return this.results;
        }

        @Nonnull
        private IntermediateResult merge(@Nonnull IntermediateResult result) {
            Set<E> mergedResults = newHashSet(this.results);
            mergedResults.addAll(getResults(result));
            return new IntermediateResultSet<E>(mergedResults);
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        private Set<E> getResults(@Nonnull IntermediateResult result) {
            return IntermediateResultSet.class.cast(result).getResults();
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
     * @since 2.0.0
     */
    public static class IntermediateResultMap<K, V> implements IntermediateResult {
        @Nonnull
        private final Logger logger = LoggerFactory.getLogger(getClass());
        @Nonnull
        private final Map<K, V> results;

        /**
         * Creates an <code>IntermediateResultMap</code> to store the given <code>Map</code>.
         *
         * @since 2.0.0
         */
        public IntermediateResultMap(@Nonnull Map<K, V> results) {
            this.results = Collections.unmodifiableMap(newHashMap(results));
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
         * Returns the stored read-only <code>Map</code>.
         *
         * @since 2.0.0
         */
        @Nonnull
        public Map<K, V> getResults() {
            return this.results;
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        private IntermediateResult merge(@Nonnull IntermediateResult result) {
            Map<K, V> mergedResults = newHashMap(getResults());
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
            return IntermediateResultMap.class.cast(result).getResults();
        }

    }

}
