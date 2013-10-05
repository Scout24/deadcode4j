package de.is24.deadcode4j;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Serves as a base class with which to analyze XML files.
 *
 * @since 1.1.1
 */
public abstract class XmlAnalyzer {

    protected final SAXParser parser;

    protected XmlAnalyzer() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            this.parser = factory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up XML parser!", e);
        }
    }

}
