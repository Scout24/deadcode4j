package de.is24.deadcode4j;

/**
 * Analyzes Spring XML files: lists the classes being referenced.
 *
 * @since 1.1.0
 */
public class SpringXmlAnalyzer extends XmlAnalyzer implements Analyzer {

    public SpringXmlAnalyzer() {
        super("_Spring_", ".xml", "beans");
        registerClassAttribute("bean", "class");
    }

}
