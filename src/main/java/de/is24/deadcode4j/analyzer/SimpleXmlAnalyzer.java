package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singleton;

/**
 * Serves as simple base class with which to analyze XML files by defining which element nodes' text or attributes
 * contain a fqcn.
 *
 * @since 1.2.0
 */
public abstract class SimpleXmlAnalyzer extends XmlAnalyzer implements Analyzer {
    private static final String NO_ATTRIBUTE = "no attribute";
    private final String dependerId;
    private final String rootElement;
    private final Map<String, String> relevantElements = newHashMap();

    /**
     * The constructor for an <code>SimpleXmlAnalyzer</code>.
     * Be sure to call {@link #registerClassElement(String)} and/or {@link #registerClassAttribute(String, String)} in the subclasses'
     * constructor.
     *
     * @param dependerId    a description of the <i>depending entity</i> with which to
     *                      call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, java.util.Collection)}
     * @param endOfFileName the file suffix used to determine if a file should be analyzed; this can be a mere file
     *                      extension like <tt>.xml</tt> or a partial path like <tt>WEB-INF/web.xml</tt>
     * @param rootElement   the expected XML root element or <code>null</code> if such an element does not exist;
     *                      i.e. there are multiple valid root elements
     * @since 1.2.0
     */
    protected SimpleXmlAnalyzer(@Nonnull String dependerId, @Nonnull String endOfFileName, @Nullable String rootElement) {
        super(endOfFileName);
        this.dependerId = dependerId;
        this.rootElement = rootElement;
    }

    @Override
    @Nonnull
    protected final DefaultHandler createHandlerFor(@Nonnull CodeContext codeContext) {
        return new XmlHandler(codeContext);
    }

    /**
     * Registers an XML element containing a fully qualified class name.
     *
     * @param elementName the name of the XML element to register
     * @since 1.2.0
     */
    protected void registerClassElement(@Nonnull String elementName) {
        this.relevantElements.put(elementName, NO_ATTRIBUTE);
    }

    /**
     * Registers an XML element's attribute denoting a fully qualified class name.
     *
     * @param elementName   the name of the XML element the attribute may occur at
     * @param attributeName the name of the attribute to register
     * @since 1.2.0
     */
    protected void registerClassAttribute(@Nonnull String elementName, @Nonnull String attributeName) {
        this.relevantElements.put(elementName, attributeName);
    }

    /**
     * @since 1.2.0
     */
    private class XmlHandler extends DefaultHandler {
        private final CodeContext codeContext;
        private boolean firstElement = true;
        private StringBuilder buffer;

        public XmlHandler(CodeContext codeContext) {
            this.codeContext = codeContext;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws StopParsing {
            if (firstElement && rootElement != null && !rootElement.equals(localName)) {
                throw new StopParsing();
            } else {
                firstElement = false;
            }
            String attributeName = relevantElements.get(localName);
            if (attributeName != null) {
                if (NO_ATTRIBUTE.equals(attributeName)) {
                    buffer = new StringBuilder(128);
                } else {
                    String className = attributes.getValue(attributeName);
                    if (className != null) {
                        codeContext.addDependencies(dependerId, singleton(className.trim()));
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
                codeContext.addDependencies(dependerId, singleton(buffer.toString()));
                buffer = null;
            }
        }

    }

}
