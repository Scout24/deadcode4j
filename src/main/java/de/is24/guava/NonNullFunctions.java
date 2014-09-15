package de.is24.guava;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import de.is24.deadcode4j.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides convenience methods for {@link de.is24.guava.NonNullFunction}.
 *
 * @since 2.0.0
 */
public final class NonNullFunctions {

    private NonNullFunctions() {}

    /**
     * Returns a <code>NonNullFunction</code> that will call the specified functions one by one until a return value is
     * <i>present</i> or the end of the call chain is reached.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static <F, T> NonNullFunction<F, Optional<T>> or(@Nonnull final NonNullFunction<F, Optional<T>>... functions) {
        return new NonNullFunction<F, Optional<T>>() {
            @Nonnull
            @Override
            public Optional<T> apply(@Nonnull F input) {
                for (int i = 0; ; ) {
                    Optional<T> result = functions[i++].apply(input);
                    if (result.isPresent() || i == functions.length) {
                        return result;
                    }
                }
            }
        };
    }

    /**
     * Transforms a <code>NonNullFunction</code> into a <code>Function</code>.
     * If a <code>Function</code>'s input is <code>null</code>, a <code>NullPointerException</code> is thrown.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static <F, T> Function<F, T> toFunction(@Nonnull final NonNullFunction<F, T> nonNullFunction) {
        return new Function<F, T>() {
            @Nullable
            @Override
            @SuppressWarnings("ConstantConditions")
            public T apply(@Nullable F input) {
                return nonNullFunction.apply(Utils.checkNotNull(input));
            }
        };
    }

}
