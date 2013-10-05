package de.is24.deadcode4j;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Analyzes <code>*.tld</code> files: lists the function, listener, tag, tag extra info & validator classes being
 * referenced.
 *
 * @since 1.1.1
 */
public class TldAnalyzer extends XmlAnalyzer implements Analyzer {
    private final XmlHandler handler;
    private final Collection<String> referencedClasses = newArrayList();

    public TldAnalyzer() {
        this.handler = new XmlHandler();
    }

    @Override
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull String fileName) {
        if (fileName.endsWith(".tld")) {
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
        codeContext.addDependencies("_tld_", this.referencedClasses);
    }

    /**
     * Used to indicate that XML parsing can be stopped.
     *
     * @since 1.1.1
     */
    private static class StopParsing extends SAXException {
    }

    private class XmlHandler extends DefaultHandler {
        private boolean firstElement = true;
        private StringBuilder buffer;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws StopParsing {
            if (firstElement && !"taglib".equals(localName)) {
                throw new StopParsing();
            } else {
                firstElement = false;
            }
            if ("function-class".equals(localName) ||
                    "listener-class".equals(localName) ||
                    "tag-class".equals(localName) ||
                    "tei-class".equals(localName) ||
                    "validator-class".equals(localName)) {
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
