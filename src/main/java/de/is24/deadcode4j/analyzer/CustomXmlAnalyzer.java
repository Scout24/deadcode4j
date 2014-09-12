package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.AnalysisSink;
import de.is24.deadcode4j.AnalyzedCode;
import de.is24.deadcode4j.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Analyzes XML files: lists the registered elements' text or attribute values as being referenced classes.
 *
 * @since 1.3
 */
public final class CustomXmlAnalyzer extends SimpleXmlAnalyzer {

    //                                                              element       [@attribute='value']    /   @attribute|text()
    private static final Pattern XPATH_PATTERN = Pattern.compile("^([^/\\[]+)(?:\\[@([^=]+)='([^']+)'\\])?/(?:@(.*)|text\\(\\))$");
    private static volatile int instanceNumber = 0; // we assign this to make sure the self check works
    private boolean dependencyWasFound = false;

    /**
     * Creates a new <code>CustomXmlAnalyzer</code>.
     * Be sure to call {@link #registerXPath(String)} after construction.
     *
     * @param dependerId    a description of the <i>depending entity</i> with which to
     *                      call {@link de.is24.deadcode4j.AnalysisContext#addDependencies(String, Iterable)}
     * @param endOfFileName the file suffix used to determine if a file should be analyzed; this can be a mere file
     *                      extension like <tt>.xml</tt> or a partial path like <tt>WEB-INF/web.xml</tt>
     * @param rootElement   the expected XML root element or <code>null</code> if such an element does not exist;
     *                      i.e. there are multiple valid root elements
     * @since 1.3
     */
    protected CustomXmlAnalyzer(@Nonnull String dependerId, @Nonnull String endOfFileName, @Nullable String rootElement) {
        super(dependerId, endOfFileName, rootElement);
    }

    /**
     * Creates a new <code>CustomXmlAnalyzer</code>.
     * Be sure to call {@link #registerXPath(String)} after construction.
     *
     * @param endOfFileName the file suffix used to determine if a file should be analyzed; this can be a mere file
     *                      extension like <tt>.xml</tt> or a partial path like <tt>WEB-INF/web.xml</tt>
     * @param rootElement   the expected XML root element or <code>null</code> if such an element does not exist;
     *                      i.e. there are multiple valid root elements
     * @since 1.3
     */
    public CustomXmlAnalyzer(@Nonnull String endOfFileName, @Nullable String rootElement) {
        this("_custom-XML#" + getNewInstanceNumber() + "_", endOfFileName, rootElement);
    }

    private static int getNewInstanceNumber() {
        return instanceNumber++;
    }

    /**
     * Register an XPath expression identifying an XML node which is to be recognized as a class being in use.
     * Supported expressions are:
     * <ul>
     * <li><i>element</i>/text()</li>
     * <li><i>element</i>/@<i>attribute</i></li>
     * </ul>
     * An element may be further restricted by defining a predicate like
     * <i>element</i>[@<i>attributeName</i>='<i>attributeValue</i>'].
     *
     * @throws IllegalArgumentException if <code>xPath</code> is not supported
     */
    public void registerXPath(String xPath) throws IllegalArgumentException {
        Matcher matcher = XPATH_PATTERN.matcher(xPath);
        checkArgument(matcher.matches(),
                "Although [" + xPath + "] may be a valid XPath expression, it is not supported!");
        String elementName = matcher.group(1);
        String attribute = matcher.group(4);
        Element element;
        if (attribute != null) {
            element = registerClassAttribute(elementName, attribute);
        } else {
            element = registerClassElement(elementName);
        }
        if (matcher.group(2) != null) {
            element.withAttributeValue(matcher.group(2), matcher.group(3));
        }
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisContext analysisContext) {
        super.finishAnalysis(analysisContext);
        Set<String> dependencies = analysisContext.getAnalyzedCode().getCodeDependencies().get(super.dependerId);
        if (!Utils.isEmpty(dependencies)) {
            dependencyWasFound = true;
        }
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisSink analysisSink, @Nonnull AnalyzedCode analyzedCode) {
        super.finishAnalysis(analysisSink, analyzedCode);
        if (!dependencyWasFound) {
            logger.warn("The {} didn't find any class to report. You should remove the configuration entry.", this);
        }
    }

}
