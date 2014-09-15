package de.is24.guava;

import javax.annotation.Nonnull;

/**
 * A <code>NonNullFunction</code> basically is a {@link com.google.common.base.Function} that
 * neither accepts <code>null</code> nor is it allowed to return <code>null</code>.
 *
 * @since 2.0.0
 */
public interface NonNullFunction<F, T> {

    /**
     * Returns the result of applying this function to the given input.
     *
     * @since 2.0.0
     */
    @Nonnull
    T apply(@Nonnull F input);

}
