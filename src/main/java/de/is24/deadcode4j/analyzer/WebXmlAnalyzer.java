package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;

/**
 * Analyzes <code>web.xml</code> files: lists the listener, filter & servlet classes being referenced.
 *
 * @since 1.2.0
 */
public final class WebXmlAnalyzer extends SimpleXmlAnalyzer implements Analyzer {

    public WebXmlAnalyzer() {
        super("_web.xml_", "web.xml", "web-app");
        registerClassElement("listener-class");
        registerClassElement("filter-class");
        registerClassElement("servlet-class");
    }

}
