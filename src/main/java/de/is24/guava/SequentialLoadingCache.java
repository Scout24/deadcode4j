package de.is24.guava;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.cache.AbstractLoadingCache;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * The <code>SequentialLoadingCache</code> is a <code>LoadingCache</code> that is intended for single-threaded access.
 * It is based on a {@link java.util.HashMap} and caches <code>null</code> values.
 *
 * @param <K> the keys' type
 * @param <V> the values' type
 * @since 2.0.0
 */
public class SequentialLoadingCache<K, V> extends AbstractLoadingCache<K, Optional<V>> {

    @Nonnull
    private final Map<K, Optional<V>> cache;
    @Nonnull
    private final Function<K, Optional<V>> cacheLoader;

    /**
     * Creates a <code>SequentialLoadingCache</code> that uses the given function to load the values.
     *
     * @since 2.0.0
     */
    protected SequentialLoadingCache(@Nonnull Map<K, Optional<V>> cache, @Nonnull Function<K, Optional<V>> cacheLoader) {
        this.cache = cache;
        this.cacheLoader = cacheLoader;
    }

    /**
     * Creates a <code>SequentialLoadingCache</code> that uses the given function to load the values.
     *
     * @since 2.0.0
     */
    public SequentialLoadingCache(@Nonnull Function<K, Optional<V>> cacheLoader) {
        this(Maps.<K, Optional<V>>newHashMap(), cacheLoader);
    }

    /**
     * Creates a <code>SequentialLoadingCache</code> that only caches one value.
     *
     * @see #SequentialLoadingCache(com.google.common.base.Function)
     * @since 2.0.0
     */
    public static <K, V> SequentialLoadingCache<K, V> createSingleValueCache(@Nonnull Function<K, Optional<V>> cacheLoader) {
        return new SequentialLoadingCache<K, V>(new HashMap<K, Optional<V>>() {
            @Override
            public Optional<V> put(K key, Optional<V> value) {
                super.clear();
                return super.put(key, value);
            }
        }, cacheLoader);
    }

    @Nonnull
    @Override
    public Optional<V> get(@Nullable K key) throws ExecutionException {
        Optional<V> value = this.cache.get(key);
        if (value == null) {
            value = cacheLoader.apply(key);
            if (value == null) {
                value = Optional.absent();
            }
            this.cache.put(key, value);
        }
        return value;
    }

    @Nonnull
    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public Optional<V> getIfPresent(@Nonnull Object key) {
        final Optional<V> value = this.cache.get(key);
        return value == null ? Optional.<V>absent() : value;
    }

}
