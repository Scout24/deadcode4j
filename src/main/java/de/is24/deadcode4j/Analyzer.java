package de.is24.deadcode4j;

/**
 * An <code>Analyzer</code> analyzes code of all flavours: java classes, spring XML files, <tt>web.xml</tt> etc.
 *
 * @since 1.0.2
 */
public interface Analyzer {
    public AnalyzedCode analyze(CodeContext codeContext);
}
