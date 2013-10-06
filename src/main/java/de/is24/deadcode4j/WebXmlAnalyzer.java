package de.is24.deadcode4j;

/**
 * Analyzes <code>web.xml</code> files: lists the listener, filter & servlet classes being referenced.
 *
 * @since 1.1.1
 */
public class WebXmlAnalyzer extends XmlAnalyzer implements Analyzer {

    public WebXmlAnalyzer() {
        super("_web.xml_", "web.xml", "web-app");
        registerClassElement("listener-class");
        registerClassElement("filter-class");
        registerClassElement("servlet-class");
    }

}
