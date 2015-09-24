package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import de.is24.deadcode4j.AnalysisContext;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getLast;
import static de.is24.deadcode4j.Utils.checkNotNull;

/**
 * TODO
 *
 * @since 2.1.0
 */
public abstract class ExtendedXmlAnalyzer extends XmlAnalyzer {
    protected final String dependerId;
    private final String rootElement;
    private final Collection<Path> pathsToMatch = new ArrayList<Path>();

    /**
     * Creates a new <code>ExtendedXmlAnalyzer</code>.
     * Be sure to call TODO
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
     * Creates a new <code>ExtendedXmlAnalyzer</code>.
     * Be sure to call TODO
     *
     * @param dependerId    a description of the <i>depending entity</i> with which to
     *                      call {@link de.is24.deadcode4j.AnalysisContext#addDependencies(String, Iterable)}
     * @param endOfFileName the file suffix used to determine if a file should be analyzed; this can be a mere file
     *                      extension like <tt>.xml</tt> or a partial path like <tt>WEB-INF/web.xml</tt>
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

    public Path anyElementNamed(String name) {
        Path path = new Path(new Element(name));
        pathsToMatch.add(path);
        return path;
    }

    /**
     * @since 2.1.0
     */
    private class XmlHandler extends DefaultHandler {
        private final AnalysisContext analysisContext;
        private final Deque<XmlElement> xmlElements = new ArrayDeque<XmlElement>();
        private StringBuilder buffer;

        public XmlHandler(AnalysisContext analysisContext) {
            this.analysisContext = analysisContext;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws StopParsing {
            xmlElements.addLast(new XmlElement(localName, attributes));
            buffer = new StringBuilder(128);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            buffer.append(new String(ch, start, length).trim());
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
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

        private final List<Element> pathElements = new ArrayList<Element>();

        Path(@Nonnull Element firstElement) {
            pathElements.add(firstElement);
        }

        public void registerTextAsClass() {
            getLast(pathElements).registerTextAsClass();
        }

        public void registerAttributeAsClass(String attributeWithClass) {
            getLast(pathElements).registerAttributeAsClass(attributeWithClass);
        }

        public Optional<String> elementEnds(Deque<XmlElement> xmlElements, Optional<String> containedText) {
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
            if (xmlElement == null) {
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
        public final Attributes attributes;

        private XmlElement(String name, Attributes attributes) {
            this.name = name;
            this.attributes = attributes;
        }

    }

    /**
     * Represents an XML element that is to be matched.
     *
     * @since 2.1.0
     */
    protected static class Element {
        private final String name;
        private boolean extractText = false;
        private String attributeToExtract;

        public Element(@Nonnull String name) {
            checkArgument(name.trim().length() > 0, "The element's [name] must be set!");
            this.name = name;
        }

        public boolean matches(XmlElement xmlElement) {
            return name.equals(xmlElement.name);
        }

        public Optional<String> extract(XmlElement xmlElement, Optional<String> containedText) {
            if (extractText) {
                return containedText;
            }
            if (attributeToExtract != null) {
                return fromNullable(xmlElement.attributes.getValue(attributeToExtract));
            }
            return absent(); // TODO or thrown an exception?
        }

        public void registerTextAsClass() {
            extractText = true;
        }

        public void registerAttributeAsClass(String attributeWithClass) {
            attributeToExtract = attributeWithClass;
        }
    }

}
