package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;

/**
 * Analyzes Spring XML files: lists the classes being referenced.
 *
 * @since 1.1.0
 */
public class SpringXmlAnalyzer extends XmlAnalyzer implements Analyzer {

    public SpringXmlAnalyzer() {
        super("_Spring-XML_", ".xml", "beans");
        registerClassAttribute("bean", "class");
    }

}
