package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import de.is24.deadcode4j.AnalysisContext;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static de.is24.deadcode4j.Utils.checkNotNull;

/**
 * Serves as base class with which to analyze XML files by defining which element nodes' text or attributes
 * contain a fqcn. It allows restricting matches to elements having a specific attribute value and certain parent
 * elements.
 *
 * @since 2.1.0
 */
public abstract class ExtendedXmlAnalyzer extends XmlAnalyzer {
    protected final String dependerId;
    private final String rootElement;
    private final Collection<Path> pathsToMatch = new ArrayList<Path>();

    /**
     * Creates a new <code>ExtendedXmlAnalyzer</code>.
     * Be sure to call {@link #anyElementNamed(String)} and so forth in the subclasses' constructor.
     *
     * @param dependerId    a description of the <i>depending entity</i> with which to
     *                      call {@link de.is24.deadcode4j.AnalysisContext#addDependencies(String, Iterable)}
     * @param endOfFileName the file suffix used to determine if a file should be analyzed; this can be a mere file
     *                      extension like <tt>.xml</tt> or a partial path like <tt>WEB-INF/web.xml</tt>
     * @param rootElement   the expected XML root element or <code>null</code> if such an element does not exist;
     *                      i.e. there are multiple valid root elements
     * @since 2.1.0
     */
    protected ExtendedXmlAnalyzer(@Nonnull String dependerId, @Nonnull String endOfFileName, @Nullable String rootElement) {
        super(endOfFileName);
        this.dependerId = checkNotNull(dependerId);
        this.rootElement = rootElement;
    }

    /**
     * Creates a new <code>ExtendedXmlAnalyzer</code> that is not restricted to a specific XML root element.
     *
     * @see #ExtendedXmlAnalyzer(String, String, String)
     * @since 2.1.0
     */
    protected ExtendedXmlAnalyzer(@Nonnull String dependerId, @Nonnull String endOfFileName) {
        this(dependerId, endOfFileName, null);
    }

    @Nonnull
    @Override
    protected final DefaultHandler createHandlerFor(@Nonnull AnalysisContext analysisContext) {
        return new XmlHandler(analysisContext);
    }

    /**
     * Sets up a path to an element to match.
     * Be sure to call {@link Path#registerTextAsClass()} or {@link Path#registerAttributeAsClass(String)} eventually.
     *
     * @param name the name of the XML element to match
     */
    public Path anyElementNamed(String name) {
        Path path = new Path(logger, new Element(logger, name));
        pathsToMatch.add(path);
        return path;
    }

    /**
     * @since 2.1.0
     */
    private class XmlHandler extends DefaultHandler {
        private final AnalysisContext analysisContext;
        private final Deque<XmlElement> xmlElements = new ArrayDeque<XmlElement>();
        private final Deque<StringBuilder> textBuffers = new ArrayDeque<StringBuilder>();
        private boolean firstElement = true;

        public XmlHandler(AnalysisContext analysisContext) {
            this.analysisContext = analysisContext;
        }

        @Override
        public void startElement(String ignoredUri, String localName, String ignoredQName, Attributes attributes) throws StopParsing {
            if (firstElement) {
                if (rootElement != null && !rootElement.equals(localName)) {
                    throw new StopParsing();
                }
                firstElement = false;
            }
            xmlElements.addLast(new XmlElement(localName, attributes));
            this.textBuffers.addLast(new StringBuilder(128));
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            this.textBuffers.getLast().append(new String(ch, start, length).trim());
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            StringBuilder buffer = textBuffers.removeLast();
            Optional<String> text = fromNullable(buffer.length() > 0 ? buffer.toString() : null);
            for (Path path : pathsToMatch) {
                Optional<String> dependee = path.elementEnds(xmlElements, text);
                if (dependee.isPresent()) {
                    analysisContext.addDependencies(dependerId, dependee.get().trim());
                }
            }
            xmlElements.removeLast();
        }

    }

    /**
     * Represents a path of XML elements to match and extract.
     *
     * @since 2.1.0
     */
    protected static class Path {
        private final Logger logger;
        private final List<Element> pathElements = new ArrayList<Element>();

        private Path(@Nonnull Logger logger, @Nonnull Element firstElement) {
            this.logger = logger;
            pathElements.add(firstElement);
        }

        /**
         * Extends the path to match with a subsequent XML element.
         *
         * @param name the name of the XML element to match
         * @since 2.1.0
         */
        public Path anyElementNamed(String name) {
            pathElements.add(new Element(logger, name));
            return this;
        }

        /**
         * Restricts the last XML element to only be matched if it has an attribute with the specified value.
         *
         * @since 2.1.0
         */
        public Path withAttributeValue(@Nonnull String attributeName, @Nonnull String requiredValue) {
            getLast(pathElements).restrictAttribute(attributeName, requiredValue);
            return this;
        }

        /**
         * Registers the last XML element's text to be treated as a fully qualified class name.
         *
         * @since 2.1.0
         */
        public void registerTextAsClass() {
            getLast(pathElements).registerTextAsClass();
        }

        /**
         * Registers an attribute of the last XML element to be treated as a fully qualified class name.
         *
         * @param attributeName the name of the attribute to register
         * @since 2.1.0
         */
        public void registerAttributeAsClass(String attributeName) {
            getLast(pathElements).registerAttributeAsClass(attributeName);
        }

        private Optional<String> elementEnds(Deque<XmlElement> xmlElements, Optional<String> containedText) {
            for (int i = xmlElements.size(); i-- > 0; ) {
                Iterator<XmlElement> xmlElementIterator = xmlElements.iterator();
                for (int j = i; j-- > 0; ) {
                    xmlElementIterator.next();
                }
                Optional<String> result = findMatch(xmlElementIterator, containedText);
                if (result.isPresent()) {
                    return result;
                }
            }
            return Optional.absent();
        }

        private Optional<String> findMatch(Iterator<XmlElement> xmlElementIterator, Optional<String> containedText) {
            XmlElement xmlElement = null;
            Element lastPathElement = null;
            for (Element pathElement : pathElements) {
                if (!xmlElementIterator.hasNext()) {
                    return absent();
                }
                xmlElement = xmlElementIterator.next();
                if (!pathElement.matches(xmlElement)) {
                    return absent();
                }
                lastPathElement = pathElement;
            }
            if (xmlElement == null || xmlElementIterator.hasNext()) {
                return absent();
            }
            return lastPathElement.extract(xmlElement, containedText);
        }

    }

    /**
     * Represents an element node found in the XML to analyze.
     *
     * @since 2.1.0
     */
    private static class XmlElement {
        public final String name;
        public final Map<String, String> attributes;

        private XmlElement(String name, Attributes attributes) {
            this.name = name;
            Map<String, String> attributeMap = newHashMapWithExpectedSize(0);
            for (int i = attributes.getLength(); i-- > 0; ) {
                attributeMap.put(attributes.getLocalName(i), attributes.getValue(i));
            }
            this.attributes = Collections.unmodifiableMap(attributeMap);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder(name);
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                result.append(attribute.getKey()).append("='").append(attribute.getValue()).append("',");
            }
            if (result.length() > name.length()) {
                result.insert(name.length(), "[");
                result.replace(result.length() - 1, result.length(), "]");
            }
            return result.toString();
        }

    }

    /**
     * Represents an XML element that is to be matched.
     *
     * @since 2.1.0
     */
    private static class Element {
        private final Logger logger;
        private final String name;
        private final Map<String, String> attributeRestrictions = newHashMapWithExpectedSize(0);
        private boolean extractText = false;
        private String attributeToExtract;

        public Element(@Nonnull Logger logger, @Nonnull String name) {
            this.logger = logger;
            checkArgument(name.trim().length() > 0, "The element's [name] must be set!");
            this.name = name;
        }

        public void restrictAttribute(@Nonnull String attribute, @Nonnull String value) {
            attributeRestrictions.put(attribute, value);
        }

        public void registerTextAsClass() {
            extractText = true;
        }

        public void registerAttributeAsClass(String attributeWithClass) {
            attributeToExtract = attributeWithClass;
        }

        public boolean matches(XmlElement xmlElement) {
            return name.equals(xmlElement.name) && matchesAttributes(xmlElement);
        }

        public Optional<String> extract(XmlElement xmlElement, Optional<String> containedText) {
            if (extractText) {
                return containedText;
            }
            if (attributeToExtract != null) {
                return fromNullable(xmlElement.attributes.get(attributeToExtract));
            }
            logger.warn("Could not extract fqcn of matched element!");
            return absent();
        }

        private boolean matchesAttributes(XmlElement xmlElement) {
            for (Map.Entry<String, String> attributeRestriction : attributeRestrictions.entrySet()) {
                String value = xmlElement.attributes.get(attributeRestriction.getKey());
                if (!attributeRestriction.getValue().equals(value)) {
                    return false;
                }
            }
            return true;
        }

    }

}
