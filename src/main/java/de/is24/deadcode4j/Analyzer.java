package de.is24.deadcode4j;

import javax.annotation.Nonnull;

/**
 * An <code>Analyzer</code> analyzes code of all flavours: java classes, spring XML files, <tt>web.xml</tt> etc.
 *
 * @since 1.0.2
 */
public interface Analyzer {

    /**
     * Perform an analysis for the specified file.
     * Results must be reported via the capabilities of the {@link CodeContext}.
     *
     * @since 1.0.2
     */
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull String fileName);

}
