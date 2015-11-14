package de.is24.deadcode4j.analyzer;
/**
 * Analyzes Logback XML configuration files:
 * <ul>
 * <li>list the property classes being referenced (<code>&lt;define ...</code>)</li>
 * <li>list the <code>appender</code> classes being referenced</li>
 * <li>list the <code>contextListener</code> classes being referenced</li>
 * <li>list the <code>statusListener</code> classes being referenced</li>
 * </ul>
 *
 * For more information have a look at chapter <a href="http://logback.qos.ch/manual/configuration.html">Chapter 3:
 * Logback configuration</a> of the Logback documentation.
 *
 * @since 2.1.0
 */
public class LogbackXmlAnalyzer extends SimpleXmlAnalyzer {

    public LogbackXmlAnalyzer() {
        super("_Logback-XML_", ".xml", "configuration");
        registerClassAttribute("appender", "class");
        registerClassAttribute("contextListener", "class");
        registerClassAttribute("define", "class");
        registerClassAttribute("statusListener", "class");
    }
}
