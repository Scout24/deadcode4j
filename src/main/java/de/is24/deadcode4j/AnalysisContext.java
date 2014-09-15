package de.is24.deadcode4j;

import de.is24.guava.NonNullFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * The <code>AnalysisContext</code> extends {@link de.is24.deadcode4j.AnalysisSink} by providing access to the
 * {@link #getModule() analyzed module} and {@link #getIntermediateResult(Object) the intermediate results} of the
 * modules it depends on. Additionally, it provides a {@link #getCache() <em>cache</em>} to use for caching calculated
 * data relevant for one context.
 *
 * @since 1.1.0
 */
public class AnalysisContext extends AnalysisSink {
    @Nonnull
    private final Map<Object, Object> cache = newHashMap();
    @Nonnull
    private final Module module;
    @Nonnull
    private final Map<Object, IntermediateResult> intermediateResults;

    /**
     * Creates a new instance of <code>AnalysisContext</code> for the specified module.
     *
     * @since 2.0.0
     */
    public AnalysisContext(@Nonnull Module module, @Nonnull Map<Object, IntermediateResult> intermediateResults) {
        this.module = module;
        this.intermediateResults = newHashMap(intermediateResults);
    }

    @Override
    public String toString() {
        return "AnalysisContext for [" + this.module + "]";
    }

    /**
     * Returns the associated <code>Module</code>.
     *
     * @since 2.0.0
     */
    @Nonnull
    public Module getModule() {
        return module;
    }

    /**
     * Returns a <code>Map</code> that can be used to cache things or pass along between analyzers.
     *
     * @return a simple {@link java.util.Map}
     */
    @Nonnull
    public Map<Object, Object> getCache() {
        return cache;
    }

    @Nonnull
    public <T> T getOrCreateCacheEntry(Object key, NonNullFunction<AnalysisContext, T> supplier) {
        @SuppressWarnings("unchecked")
        T entry = (T) getCache().get(key);
        if (entry == null) {
            entry = supplier.apply(this);
            getCache().put(key, entry);
        }
        return entry;
    }

    @Nullable
    public IntermediateResult getIntermediateResult(@Nonnull Object key) {
        return this.intermediateResults.get(key);
    }

}
