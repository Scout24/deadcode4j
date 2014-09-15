package de.is24.deadcode4j.analyzer;

/**
 * Analyzes <a href="http://www.eclipse.org/jetty/configure_9_0.dtd">Jetty XML configuration</a> files.
 * Reports the {@code class} and {@code type} attributes as classes being referenced.
 *
 * @since 2.0.0
 */
public class JettyXmlAnalyzer extends SimpleXmlAnalyzer {

    public JettyXmlAnalyzer() {
        super("_Jetty-XML_", ".xml", "Configure");
        registerClassAttribute("Arg", "type");
        registerClassAttribute("Array", "type");
        registerClassAttribute("Call", "class");
        registerClassAttribute("Configure", "class");
        registerClassAttribute("Get", "class");
        registerClassAttribute("Item", "type");
        registerClassAttribute("New", "class");
        registerClassAttribute("Put", "type");
        registerClassAttribute("Set", "class");
        registerClassAttribute("Set", "type");
    }

}
