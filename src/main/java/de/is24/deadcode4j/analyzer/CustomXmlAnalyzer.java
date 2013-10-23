package de.is24.deadcode4j.analyzer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes XML files: lists the registered elements' text or attribute values as being referenced classes.
 *
 * @since 1.3.0
 */
public class CustomXmlAnalyzer extends XmlAnalyzer {

    private static final Pattern XPATH_PATTERN = Pattern.compile("^(?<element>[^/]+)/(?:@(?<attribute>.*)|text\\(\\))$");

    /**
     * Creates a new <code>CustomXmlAnalyzer</code>.
     * Be sure to call {@link #registerXPath(String)} after construction.
     *
     * @param dependerId    a description of the <i>depending entity</i> with which to
     *                      call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, java.util.Collection)}
     * @param endOfFileName the file suffix used to determine if a file should be analyzed; this can be a mere file
     *                      extension like <tt>.xml</tt> or a partial path like <tt>WEB-INF/web.xml</tt>
     * @param rootElement   the expected XML root element or <code>null</code> if such an element does not exist;
     *                      i.e. there are multiple valid root elements
     * @since 1.3.0
     */
    public CustomXmlAnalyzer(@Nonnull String dependerId, @Nonnull String endOfFileName, @Nullable String rootElement) {
        super(dependerId, endOfFileName, rootElement);
    }

    /**
     * Register an XPath expression identifying an XML node which is to be recognized as a class being in use.
     * Supported expressions are:
     * <ul>
     * <li><i>element</i>/text()</li>
     * <li><i>element</i>/@<i>attribute</i></li>
     * </ul>
     *
     * @throws IllegalArgumentException if <code>xPath</code> is not supported
     */
    public void registerXPath(String xPath) throws IllegalArgumentException {
        Matcher matcher = XPATH_PATTERN.matcher(xPath);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Although [" + xPath + "] may be a valid XPath expression, it is not supported!");
        }
        String element = matcher.group("element");
        String attribute = matcher.group("attribute");
        if (attribute != null) {
            registerClassAttribute(element, attribute);
        } else {
            registerClassElement(element);
        }
    }

}
