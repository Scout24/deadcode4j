package de.is24.deadcode4j.analyzer;

/**
 * <p>Analyzes Logback XML configuration files (only files named <code>logback.xml</code>) by reporting every
 * <code>class</code> and <code>actionClass</code> attribute as being <i>live code</i>.</p>
 * To understand why this wildcard approach is chosen, have a look at section
 * <a href="http://logback.qos.ch/manual/configuration.html#syntax">Configuration file syntax</a> and
 * <a href="http://logback.qos.ch/manual/onJoran.html">Joran</a> of the Logback documentation.
 *
 * @since 2.2.0
 */
public class LogbackXmlAnalyzer extends ExtendedXmlAnalyzer {

    public LogbackXmlAnalyzer() {
        super("_Logback-XML_", "logback.xml", "configuration");
        anyElement().registerAttributeAsClass("actionClass");
        anyElement().registerAttributeAsClass("class");
    }

}
