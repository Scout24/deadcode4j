package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import de.is24.deadcode4j.AnalysisContext;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.*;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static de.is24.deadcode4j.Utils.checkNotNull;
import static de.is24.deadcode4j.Utils.isNotBlank;
import static java.util.Collections.emptyMap;

/**
 * Serves as base class with which to analyze XML files by defining which element nodes' text or attributes
 * contain a fqcn. It allows restricting matches to elements having a specific attribute value and certain parent
 * elements.
 *
 * @since 2.1.0
 */
public abstract class ExtendedXmlAnalyzer extends XmlAnalyzer {
    @Nonnull
    protected final String dependerId;
    @Nullable
    private final String rootElement;
    @Nonnull
    private final Collection<XPath> pathsToMatch = new ArrayList<XPath>();

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

    @Override
    public String toString() {
        StringBuilder buffy = new StringBuilder(1024).append(super.toString()).append("; registered XPaths are:");
        for (XPath xPath : pathsToMatch) {
            buffy.append('\n');
            if (rootElement != null) {
                buffy.append('/').append(rootElement);
            }
            buffy.append(xPath);
        }
        return buffy.toString();
    }

    /**
     * Sets up a path to an element to match.
     * Be sure to call {@link Path#registerTextAsClass()} or {@link Path#registerAttributeAsClass(String)} eventually.
     *
     * @param name the name of the XML element to match
     * @since 2.1.0
     */
    @Nonnull
    public final Path anyElementNamed(@Nonnull String name) {
        return new Path(new Element(name));
    }

    /**
     * Sets up a path to match any element.
     * Be sure to call {@link Path#registerTextAsClass()} or {@link Path#registerAttributeAsClass(String)} eventually.
     *
     * @since 2.1.0
     */
    public Path anyElement() {
        return new Path(new Element());
    }

    @Nonnull
    @Override
    protected final DefaultHandler createHandlerFor(@Nonnull AnalysisContext analysisContext) {
        return new XmlHandler(analysisContext);
    }

    /**
     * Represents an element node found in the XML to analyze.
     *
     * @since 2.1.0
     */
    @Immutable
    protected static class XmlElement {
        @Nonnull
        public final String name;
        @Nonnull
        private final Map<String, String> attributes;

        public XmlElement(@Nonnull String name, @Nonnull Attributes attributes) {
            this.name = name;
            Map<String, String> attributeMap = newHashMapWithExpectedSize(attributes.getLength());
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

        @Nonnull
        public Optional<String> getAttribute(@Nonnull String name) {
            return fromNullable(attributes.get(name));
        }

    }

    /**
     * A <code>DependeeExtractor</code> is used to extract the dependee to report
     * if a (sub-)tree of <code>XmlElement</code>s is found that matches against an XPath-like expression.
     *
     * @since 2.1.0
     */
    protected interface DependeeExtractor {

        /**
         * Called to extract the dependee to report.
         *
         * @param xmlElements   the XML tree that matched for a given XPath-like expression
         * @param containedText the text of the last XML element
         */
        @Nonnull
        Optional<String> extractDependee(@Nonnull Iterable<XmlElement> xmlElements, @Nonnull Optional<String> containedText);

        /**
         * Subclasses are requested to override this, as the outcome will be appended to {@link XPath}'s string representation.
         */
        @Override
        String toString();
    }

    /**
     * Represents an XPath equivalent to match against an {@link XmlElement} tree.
     * This class handles the matching, extracting a class to report is delegated to the provided {@link DependeeExtractor}.
     *
     * @since 2.1.0
     */
    protected static class XPath {
        @Nonnull
        private final Path path;
        @Nonnull
        private final DependeeExtractor dependeeExtractor;

        /**
         * Creates a new <code>XPath</code> expression for the specified path.
         *
         * @since 2.1.0
         */
        protected XPath(@Nonnull Path path, @Nonnull DependeeExtractor dependeeExtractor) {
            this.path = path;
            this.dependeeExtractor = dependeeExtractor;
        }

        @Override
        public String toString() {
            return path.toString() + dependeeExtractor.toString();
        }

        @Nonnull
        Optional<String> matchAndExtract(@Nonnull Deque<XmlElement> xmlElements, @Nonnull Optional<String> containedText) {
            for (int i = xmlElements.size(); i-- > 0; ) {
                Iterable<XmlElement> partialPath = Iterables.skip(xmlElements, i);
                if (path.matches(partialPath)) {
                    return dependeeExtractor.extractDependee(partialPath, containedText);
                }
            }
            return Optional.absent();
        }

    }

    /**
     * Represents an XML element that is to be matched.
     *
     * @since 2.1.0
     */
    @Immutable
    private static class Element {
        @Nonnull
        private final Optional<String> name;
        @Nonnull
        private final Map<String, String> attributeRestrictions;

        Element() {
            this.name = Optional.absent();
            attributeRestrictions = emptyMap();
        }

        Element(@Nonnull String name) {
            checkArgument(isNotBlank(name), "The Element's [name] must be set!");
            this.name = Optional.of(name);
            attributeRestrictions = emptyMap();
        }

        private Element(@Nonnull Element original, @Nonnull String attribute, @Nonnull String value) {
            name = original.name;
            attributeRestrictions = new HashMap<String, String>(original.attributeRestrictions);
            attributeRestrictions.put(attribute, value);
        }

        @Override
        public String toString() {
            StringBuilder buffy = new StringBuilder(name.or("*"));
            if (!attributeRestrictions.isEmpty()) {
                buffy.append('[');
                for (Map.Entry<String, String> entry : attributeRestrictions.entrySet()) {
                    buffy.append('@').append(entry.getKey()).append("='").append(entry.getValue()).append("' and ");
                }
                buffy.setLength(buffy.length() - 5);
                buffy.append(']');
            }
            return buffy.toString();
        }

        @Nonnull
        Element restrictAttribute(@Nonnull String attribute, @Nonnull String value) {
            return new Element(this, attribute, value);
        }

        boolean matches(@Nonnull XmlElement xmlElement) {
            if (name.isPresent() && !name.get().equals(xmlElement.name)) {
                return false;
            }
            return matchesAttributes(xmlElement);
        }

        private boolean matchesAttributes(@Nonnull XmlElement xmlElement) {
            for (Map.Entry<String, String> attributeRestriction : attributeRestrictions.entrySet()) {
                Optional<String> value = xmlElement.getAttribute(attributeRestriction.getKey());
                if (!(value.isPresent() && attributeRestriction.getValue().equals(value.get()))) {
                    return false;
                }
            }
            return true;
        }

    }

    /**
     * @since 2.1.0
     */
    private class XmlHandler extends DefaultHandler {
        @Nonnull
        private final AnalysisContext analysisContext;
        @Nonnull
        private final Deque<XmlElement> xmlElements = new ArrayDeque<XmlElement>();
        @Nonnull
        private final Deque<StringBuilder> textBuffers = new ArrayDeque<StringBuilder>();
        private boolean firstElement = true;

        public XmlHandler(@Nonnull AnalysisContext analysisContext) {
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
            textBuffers.addLast(new StringBuilder(128));
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            this.textBuffers.getLast().append(new String(ch, start, length).trim());
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            StringBuilder buffer = textBuffers.removeLast();
            Optional<String> text = fromNullable(buffer.length() > 0 ? buffer.toString() : null);
            for (XPath xPath : pathsToMatch) {
                Optional<String> dependee = xPath.matchAndExtract(xmlElements, text);
                if (dependee.isPresent()) {
                    analysisContext.addDependencies(dependerId, dependee.get().trim());
                }
            }
            xmlElements.removeLast();
        }

    }

    /**
     * Represents a path of XML elements to match.
     *
     * @since 2.1.0
     */
    @Immutable
    protected class Path {
        @Nonnull
        private final List<Element> pathElements;

        Path(@Nonnull Element firstElement) {
            pathElements = Collections.singletonList(firstElement);
        }

        private Path(@Nonnull Path original, @Nonnull String elementName) {
            pathElements = new ArrayList<Element>(original.pathElements);
            pathElements.add(new Element(elementName));
        }

        private Path(@Nonnull Path original, @Nonnull Element lastElementReplacement) {
            pathElements = new ArrayList<Element>(original.pathElements);
            pathElements.set(pathElements.size() - 1, lastElementReplacement);
        }

        @Override
        public String toString() {
            StringBuilder buffy = new StringBuilder("//");
            for (Element pathElement : pathElements) {
                buffy.append(pathElement).append('/');
            }
            buffy.setLength(buffy.length() - 1);
            return buffy.toString();
        }

        /**
         * Extends the path to match with a subsequent XML element.
         *
         * @param name the name of the XML element to match
         * @since 2.1.0
         */
        @Nonnull
        public Path anyElementNamed(@Nonnull String name) {
            return new Path(this, name);
        }

        /**
         * Restricts the last XML element to only be matched if it has an attribute with the specified value.
         *
         * @since 2.1.0
         */
        @Nonnull
        public Path withAttributeValue(@Nonnull String attributeName, @Nonnull String requiredValue) {
            return new Path(this, getLast(pathElements).restrictAttribute(attributeName, requiredValue));
        }

        /**
         * Registers the specified <code>DependeeExtractor</code> to be called if this XML path is found.
         *
         * @see #registerTextAsClass()
         * @see #registerAttributeAsClass(String)
         * @since 2.1.0
         */
        public void registerDependeeExtractor(DependeeExtractor dependeeExtractor) {
            pathsToMatch.add(new XPath(this, dependeeExtractor));
        }

        /**
         * Registers the last XML element's text to be treated as a fully qualified class name.
         *
         * @since 2.1.0
         */
        public void registerTextAsClass() {
            registerDependeeExtractor(new DependeeExtractor() {
                @Override
                public String toString() {
                    return "[text()]";
                }
                @Nonnull
                @Override
                public Optional<String> extractDependee(@Nonnull Iterable<XmlElement> xmlElements, @Nonnull Optional<String> containedText) {
                    return containedText;
                }
            });
        }

        /**
         * Registers an attribute of the last XML element to be treated as a fully qualified class name.
         *
         * @param attributeName the name of the attribute to register
         * @since 2.1.0
         */
        public void registerAttributeAsClass(final String attributeName) {
            registerDependeeExtractor(new DependeeExtractor() {
                @Override
                public String toString() {
                    return "/@" + attributeName;
                }

                @Nonnull
                @Override
                public Optional<String> extractDependee(@Nonnull Iterable<XmlElement> xmlElements, @Nonnull Optional<String> containedText) {
                    return getLast(xmlElements).getAttribute(attributeName);
                }
            });
        }

        boolean matches(@Nonnull Iterable<XmlElement> xmlElements) {
            if (pathElements.size() != Iterables.size(xmlElements)) {
                return false;
            }

            Iterator<XmlElement> xmlElementIterator = xmlElements.iterator();
            for (Element pathElement : pathElements) {
                if (!pathElement.matches(xmlElementIterator.next())) {
                    return false;
                }
            }
            return true;
        }
    }

}
