package de.is24.guava;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides convenience methods for {@link de.is24.guava.NonNullFunction}.
 *
 * @since 1.6
 */
public final class NonNullFunctions {

    private NonNullFunctions() {}

    /**
     * Transforms a <code>NonNullFunction</code> into a <code>Function</code>.
     * If a <code>Function</code>'s input is <code>null</code>, a <code>NullPointerException</code> is thrown.
     *
     * @since 1.6
     */
    @Nonnull
    public static <F, T> Function<F, T> toFunction(@Nonnull final NonNullFunction<F, T> nonNullFunction) {
        return new Function<F, T>() {
            @Nullable
            @Override
            @SuppressWarnings("ConstantConditions")
            public T apply(@Nullable F input) {
                return nonNullFunction.apply(Preconditions.checkNotNull(input));
            }
        };
    }

}
