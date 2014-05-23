package de.is24.javaparser;

import com.google.common.base.Predicate;
import japa.parser.ast.ImportDeclaration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides convenience methods for dealing with {@link japa.parser.ast.ImportDeclaration}s.
 *
 * @since 1.6
 */
public final class ImportDeclarations {
    private ImportDeclarations() {
    }

    /**
     * Returns a <code>Predicate</code> that evaluates to <code>true</code> if the <code>ImportDeclaration</code> being
     * tested is an asterisk import.
     *
     * @since 1.6
     */
    @Nonnull
    public static Predicate<? super ImportDeclaration> isAsterisk() {
        return new Predicate<ImportDeclaration>() {
            @Override
            public boolean apply(@Nullable ImportDeclaration input) {
                return input != null && input.isAsterisk();
            }
        };
    }

    /**
     * Returns a <code>Predicate</code> that evaluates to <code>true</code> if the <code>ImportDeclaration</code> being
     * tested is a static import.
     *
     * @since 1.6
     */
    @Nonnull
    public static Predicate<? super ImportDeclaration> isStatic() {
        return new Predicate<ImportDeclaration>() {
            @Override
            public boolean apply(@Nullable ImportDeclaration input) {
                return input != null && input.isStatic();
            }
        };
    }

}
