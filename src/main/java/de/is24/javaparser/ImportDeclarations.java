package de.is24.javaparser;

import com.google.common.base.Predicate;
import com.github.javaparser.ast.ImportDeclaration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static de.is24.deadcode4j.Utils.checkNotNull;

/**
 * Provides convenience methods for dealing with {@link ImportDeclaration}s.
 *
 * @since 2.0.0
 */
public final class ImportDeclarations {

    private ImportDeclarations() {}

    /**
     * Returns a <code>Predicate</code> that evaluates to <code>true</code> if the <code>ImportDeclaration</code> being
     * tested is an asterisk import.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static Predicate<? super ImportDeclaration> isAsterisk() {
        return new Predicate<ImportDeclaration>() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public boolean apply(@Nullable ImportDeclaration input) {
                return checkNotNull(input).isAsterisk();
            }
        };
    }

    /**
     * Returns a <code>Predicate</code> that evaluates to <code>true</code> if the <code>ImportDeclaration</code> being
     * tested is a static import.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static Predicate<? super ImportDeclaration> isStatic() {
        return new Predicate<ImportDeclaration>() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public boolean apply(@Nullable ImportDeclaration input) {
                return checkNotNull(input).isStatic();
            }
        };
    }

    /**
     * Returns a <code>Predicate</code> that evaluates to <code>true</code> if the last qualifier of the
     * <code>ImportDeclaration</code> being tested matches the given String.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static Predicate<? super ImportDeclaration> refersTo(@Nonnull final String lastQualifier) {
        return new Predicate<ImportDeclaration>() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public boolean apply(@Nullable ImportDeclaration input) {
                return lastQualifier.equals(checkNotNull(input).getName().getName());
            }
        };
    }

}
