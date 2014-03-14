package de.is24.deadcode4j.analyzer;

import javax.annotation.Nonnull;

/**
 * Analyzes class files: marks a class as being in use if it explicitly implements one of the specified interfaces.
 *
 * @since 1.4
 */
public final class CustomInterfacesAnalyzer extends InterfacesAnalyzer {

    /**
     * Creates a new <code>CustomInterfacesAnalyzer</code>.
     *
     * @param customInterfaces a list of fully qualified interface names indicating that the implementing class is in use
     * @since 1.4
     */
    public CustomInterfacesAnalyzer(@Nonnull Iterable<String> customInterfaces) {
        super("_custom-interfaces_", customInterfaces);
    }

}
