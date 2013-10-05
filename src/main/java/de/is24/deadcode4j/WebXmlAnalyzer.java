package de.is24.deadcode4j;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Analyzes <code>web.xml</code> files: lists the listener, filter & servlet classes being referenced.
 *
 * @since 1.1.1
 */
public class WebXmlAnalyzer implements Analyzer {
    private final SAXParser parser;
    private final XmlHandler handler;
    private final Collection<String> referencedClasses = newArrayList();

    public WebXmlAnalyzer() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            this.parser = factory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up XML parser!", e);
        }
        this.handler = new XmlHandler();
    }

    @Override
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull String fileName) {
        if (fileName.endsWith("web.xml")) {
            analyzeXmlFile(codeContext, fileName);
        }
    }

    private void analyzeXmlFile(@Nonnull CodeContext codeContext, @Nonnull String file) {
        this.referencedClasses.clear();
        this.handler.reset();
        try {
            parser.parse(codeContext.getClassLoader().getResourceAsStream(file), handler);
        } catch (StopParsing command) {
            return;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse [" + file + "]!", e);
        }
        codeContext.addDependencies("_web.xml_", this.referencedClasses);
    }

    /**
     * Used to indicate that XML parsing can be stopped.
     *
     * @since 1.1.0
     */
    private static class StopParsing extends SAXException {
    }

    private class XmlHandler extends DefaultHandler {
        private boolean firstElement = true;
        private StringBuilder buffer;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws StopParsing {
            if (firstElement && !"web-app".equals(localName)) {
                throw new StopParsing();
            } else {
                firstElement = false;
            }
            if ("listener-class".equals(localName)||"filter-class".equals(localName)||"servlet-class".equals(localName)) {
                buffer = new StringBuilder(128);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (buffer != null) {
                buffer.append(new String(ch, start, length).trim());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (buffer != null) {
                referencedClasses.add(buffer.toString());
                buffer = null;
            }
        }

        public void reset() {
            this.firstElement = true;
            this.buffer = null;
        }

    }

}
