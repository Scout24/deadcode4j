package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import de.is24.deadcode4j.AnalysisContext;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.isNotBlank;

/**
 * Serves as simple base class with which to analyze XML files by defining which element nodes' text or attributes
 * contain a fqcn.
 *
 * @since 1.2.0
 */
public abstract class SimpleXmlAnalyzer extends XmlAnalyzer {
    protected final String dependerId;
    private final String rootElement;
    private final Set<Element> registeredElements = newHashSet();

    /**
     * The constructor for a <code>SimpleXmlAnalyzer</code>.
     * Be sure to call {@link #registerClassElement(String)} and/or {@link #registerClassAttribute(String, String)} in the subclasses'
     * constructor.
     *
     * @param dependerId    a description of the <i>depending entity</i> with which to
     *                      call {@link de.is24.deadcode4j.AnalysisContext#addDependencies(String, Iterable)}
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
    public String toString() {
        String description = super.toString();
        if (this.rootElement == null) {
            return description;
        }
        return description + " with root Element <" + this.rootElement + ">";
    }

    @Override
    @Nonnull
    protected final DefaultHandler createHandlerFor(@Nonnull AnalysisContext analysisContext) {
        return new XmlHandler(analysisContext);
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
            checkArgument(isNotBlank(name), "The element's [name] must be set!");
            this.name = name;
        }

        /**
         * Restricts the element to only be examined if it has an attribute with the specified value.
         *
         * @since 1.5
         */
        public Element withAttributeValue(@Nonnull String attributeName, @Nonnull String requiredValue) {
            checkArgument(isNotBlank(attributeName), "[attributeName] must be given!");
            checkArgument(isNotBlank(requiredValue), "[requiredValue] must be given!");
            this.requiredAttributeValues.put(attributeName, requiredValue);
            return this;
        }

        void reportTextAsClass() {
            this.reportTextAsClass = true;
        }

        boolean shouldReportTextAsClass() {
            return this.reportTextAsClass;
        }

        String getAttributeToReportAsClass() {
            return this.attributeToReportAsClass;
        }

        void setAttributeToReportAsClass(@Nonnull String attributeName) {
            checkArgument(isNotBlank(attributeName), "[attributeName] must be given!");
            checkState(this.attributeToReportAsClass == null,
                    "Already registered [" + this.attributeToReportAsClass + "] as attribute to report as class!");
            this.attributeToReportAsClass = attributeName;
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

    /**
     * @since 1.2.0
     */
    private class XmlHandler extends DefaultHandler {
        private final AnalysisContext analysisContext;
        private boolean firstElement = true;
        private final Deque<Optional<StringBuilder>> textBuffers = new ArrayDeque<Optional<StringBuilder>>();

        public XmlHandler(AnalysisContext analysisContext) {
            this.analysisContext = analysisContext;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws StopParsing {
            if (firstElement && rootElement != null && !rootElement.equals(localName)) {
                throw new StopParsing();
            } else {
                firstElement = false;
            }
            boolean recordText = false;
            for (Element registeredElement : registeredElements) {
                if (registeredElement.matches(localName, attributes)) {
                    if (registeredElement.shouldReportTextAsClass()) {
                        recordText = true;
                    }
                    String attributeToReportAsClass = registeredElement.getAttributeToReportAsClass();
                    if (attributeToReportAsClass != null) {
                        String className = attributes.getValue(attributeToReportAsClass);
                        if (className != null) {
                            analysisContext.addDependencies(dependerId, className.trim());
                        }
                    }
                }
            }
            textBuffers.addLast(recordText ? Optional.of(new StringBuilder()) : Optional.<StringBuilder>absent());
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            Optional<StringBuilder> buffer = textBuffers.getLast();
            if (buffer.isPresent()) {
                buffer.get().append(new String(ch, start, length).trim());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            Optional<StringBuilder> optionalBuffer = textBuffers.removeLast();
            if (!optionalBuffer.isPresent()) {
                return;
            }
            StringBuilder buffer = optionalBuffer.get();
            if (buffer.length() > 0) {
                analysisContext.addDependencies(dependerId, buffer.toString());
            }
        }

    }

}
