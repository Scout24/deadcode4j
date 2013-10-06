package de.is24.deadcode4j;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Serves as a base class with which to analyze XML files.
 *
 * @since 1.1.1
 */
public abstract class XmlAnalyzer implements Analyzer {

    private final SAXParser parser;
    private final XmlHandler handler;
    private final String dependerId;
    private final String endOfFileName;
    private final String rootElement;
    private final Map<String, String> relevantElements = newHashMap();
    private final Collection<String> referencedClasses = newArrayList();

    /**
     * The constructor for an <code>XmlAnalyzer</code>.
     * Be sure to call {@link #registerClassElement(String)} and/or {@link #registerClassAttribute(String, String)} in the subclasses'
     * constructor.
     *
     * @param dependerId    a description of the <i>depending entity</i> with which to
     *                      call {@link CodeContext#addDependencies(String, java.util.Collection)}
     * @param endOfFileName the file suffix used to determine if a file should be analyzed; this can be a mere file
     *                      extension like <tt>.xml</tt> or a partial path like <tt>WEB-INF/web.xml</tt>
     * @param rootElement   the expected XML root element or <code>null</code> if such an element does not exist;
     *                      i.e. there are multiple valid root elements
     * @since 1.1.1
     */
    protected XmlAnalyzer(@Nonnull String dependerId, @Nonnull String endOfFileName, @Nullable String rootElement) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            this.parser = factory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up XML parser!", e);
        }
        this.handler = new XmlHandler();
        this.dependerId = dependerId;
        this.endOfFileName = endOfFileName;
        this.rootElement = rootElement;
    }

    @Override
    public final void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull String fileName) {
        if (fileName.endsWith(endOfFileName)) {
            analyzeXmlFile(codeContext, fileName);
        }
    }

    /**
     * Registers an XML element containing a fully qualified class name.
     *
     * @param elementName the name of the XML element to register
     */
    protected void registerClassElement(@Nonnull String elementName) {
        this.relevantElements.put(elementName, "");
    }

    /**
     * Registers an XML element's attribute denoting a fully qualified class name.
     *
     * @param elementName the name of the XML element the attribute may occur at
     * @param attributeName the name of the attribute to register
     */
    protected void registerClassAttribute(@Nonnull String elementName, @Nonnull String attributeName) {
        this.relevantElements.put(elementName, attributeName);
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
        codeContext.addDependencies(dependerId, this.referencedClasses);
    }

    /**
     * Used to indicate that XML parsing can be stopped.
     *
     * @since 1.1.1
     */
    private static class StopParsing extends SAXException {
    }

    /**
     * Used to indicate that XML parsing can be stopped.
     *
     * @since 1.1.1
     */
    private class XmlHandler extends DefaultHandler {
        private boolean firstElement = true;
        private StringBuilder buffer;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws StopParsing {
            if (firstElement && rootElement != null && !rootElement.equals(localName)) {
                throw new StopParsing();
            } else {
                firstElement = false;
            }
            String attributeName = relevantElements.get(localName);
            if (attributeName != null) {
                if ("".equals(attributeName)) {
                    buffer = new StringBuilder(128);
                } else {
                    String className = attributes.getValue(attributeName);
                    if (className != null) {
                        referencedClasses.add(className);
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (buffer != null) {
                buffer.append(new String(ch, start, length).trim());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
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