package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Serves as simple base class with which to analyze XML files by defining which element nodes' text or attributes
 * contain a fqcn.
 *
 * @since 1.2.0
 */
public abstract class SimpleXmlAnalyzer extends XmlAnalyzer implements Analyzer {
    private final String dependerId;
    private final String rootElement;
    private final Set<Element> registeredElements = newHashSet();

    /**
     * The constructor for a <code>SimpleXmlAnalyzer</code>.
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
     * Registers an XML element containing a fully qualified class name. The returned <code>Element</code> can be
     * {@link Element#withAttributeValue(String, String) restricted further}.
     *
     * @param elementName the name of the XML element to register
     * @since 1.2.0
     */
    protected Element registerClassElement(@Nonnull String elementName) {
        Element element = new Element(elementName);
        element.reportTextAsClass();
        this.registeredElements.add(element);
        return element;
    }

    /**
     * Registers an XML element's attribute denoting a fully qualified class name. The returned <code>Element</code> can
     * be {@link Element#withAttributeValue(String, String) restricted further}.
     *
     * @param elementName   the name of the XML element the attribute may occur at
     * @param attributeName the name of the attribute to register
     * @since 1.2.0
     */
    protected Element registerClassAttribute(@Nonnull String elementName, @Nonnull String attributeName) {
        Element element = new Element(elementName);
        element.setAttributeToReportAsClass(attributeName);
        this.registeredElements.add(element);
        return element;
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
            for (Element registeredElement : registeredElements) {
                if (registeredElement.matches(localName, attributes)) {
                    if (registeredElement.shouldReportTextAsClass()) {
                        buffer = new StringBuilder(128);
                    }
                    String attributeToReportAsClass = registeredElement.getAttributeToReportAsClass();
                    if (attributeToReportAsClass != null) {
                        String className = attributes.getValue(attributeToReportAsClass);
                        if (className != null) {
                            codeContext.addDependencies(dependerId, className.trim());
                        }
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
            if (buffer != null && buffer.length() > 0) {
                codeContext.addDependencies(dependerId, buffer.toString());
                buffer = null;
            }
        }

    }

    /**
     * Represents an XML element that is to be examined.
     *
     * @since 1.5
     */
    protected static class Element {

        private final String name;
        private final Map<String, String> requiredAttributeValues = newHashMap();
        private boolean reportTextAsClass = false;
        private String attributeToReportAsClass;

        public Element(@Nonnull String name) {
            if (name.trim().length() == 0) {
                throw new IllegalArgumentException("The element's [name] must be set!");
            }
            this.name = name;
        }

        /**
         * Restricts the element to only be examined if it has an attribute with the specified value.
         *
         * @since 1.5
         */
        public Element withAttributeValue(@Nonnull String attributeName, @Nonnull String requiredValue) {
            if (attributeName.trim().length() == 0) {
                throw new IllegalArgumentException("[attributeName] must be given!");
            }
            if (requiredValue.trim().length() == 0) {
                throw new IllegalArgumentException("[requiredValue] must be given!");
            }
            this.requiredAttributeValues.put(attributeName, requiredValue);
            return this;
        }

        void reportTextAsClass() {
            this.reportTextAsClass = true;
        }

        boolean shouldReportTextAsClass() {
            return this.reportTextAsClass;
        }

        void setAttributeToReportAsClass(@Nonnull String attributeName) {
            if (attributeName.trim().length() == 0) {
                throw new IllegalArgumentException("[attributeName] must be given!");
            }
            if (this.attributeToReportAsClass != null)
                throw new IllegalStateException("Already registered [" + this.attributeToReportAsClass
                        + "] as attribute to report as class!");
            this.attributeToReportAsClass = attributeName;
        }

        String getAttributeToReportAsClass() {
            return this.attributeToReportAsClass;
        }

        boolean matches(String localName, Attributes attributes) {
            if (!name.equals(localName)) {
                return false;
            }
            for (Map.Entry<String, String> entry : this.requiredAttributeValues.entrySet()) {
                String expectedValue = entry.getValue();
                String currentValue = attributes.getValue(entry.getKey());
                if (!expectedValue.equals(currentValue)) {
                    return false;
                }
            }

            return true;
        }

    }

}
