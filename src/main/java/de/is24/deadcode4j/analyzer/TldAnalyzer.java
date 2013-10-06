package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;

/**
 * Analyzes <code>*.tld</code> files: lists the function, listener, tag, tag extra info & validator classes being
 * referenced.
 *
 * @since 1.1.1
 */
public class TldAnalyzer extends XmlAnalyzer implements Analyzer {

    public TldAnalyzer() {
        super("_tld_", ".tld", "taglib");
        registerClassElement("function-class");
        registerClassElement("listener-class");
        registerClassElement("tag-class");
        registerClassElement("tei-class");
        registerClassElement("validator-class");
    }

}
