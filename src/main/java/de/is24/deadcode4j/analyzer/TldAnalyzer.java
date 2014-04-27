package de.is24.deadcode4j.analyzer;

/**
 * Analyzes <code>*.tld</code> files: lists the function, listener, tag, tag extra info & validator classes being
 * referenced.
 *
 * @since 1.2.0
 */
public final class TldAnalyzer extends SimpleXmlAnalyzer {

    public TldAnalyzer() {
        super("_tld_", ".tld", "taglib");
        registerClassElement("function-class");
        registerClassElement("listener-class");
        registerClassElement("tag-class");
        registerClassElement("tei-class");
        registerClassElement("validator-class");
        // this was valid at least for J2EE 1.2, see http://java.sun.com/j2ee/dtds/web-jsptaglibrary_1_1.dtd
        registerClassElement("tagclass");
        registerClassElement("teiclass");
    }

}
