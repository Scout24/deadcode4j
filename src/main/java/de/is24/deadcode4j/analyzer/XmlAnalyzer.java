package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static de.is24.deadcode4j.Utils.isNotBlank;

/**
 * Serves as a base class with which to analyze XML files.
 *
 * @since 1.2.0
 */
public abstract class XmlAnalyzer extends AnalyzerAdapter {
    private final SAXParser parser;
    private final String endOfFileName;

    /**
     * The constructor for an <code>XmlAnalyzer</code>.
     *
     * @param endOfFileName the file suffix used to determine if a file should be analyzed; this can be a mere file
     *                      extension like <tt>.xml</tt> or a partial path like <tt>WEB-INF/web.xml</tt>
     * @since 1.4
     */
    protected XmlAnalyzer(@Nonnull String endOfFileName) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            this.parser = factory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up XML parser!", e);
        }
        checkArgument(isNotBlank(endOfFileName), "[endOfFileName] must be set!");
        this.endOfFileName = endOfFileName;
    }

    @Override
    public String toString() {
        return super.toString() + " analyzing [" + endOfFileName + "] files";
    }

    @Override
    public final void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File file) {
        if (file.getName().endsWith(endOfFileName)) {
            logger.debug("Analyzing XML file [{}]...", file);
            analyzeXmlFile(analysisContext, file);
        }
    }

    /**
     * This method is called to provide a <code>DefaultHandler</code> for each file being processed.
     *
     * @since 1.4
     */
    @Nonnull
    protected abstract DefaultHandler createHandlerFor(@Nonnull AnalysisContext analysisContext);

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private void analyzeXmlFile(@Nonnull AnalysisContext analysisContext, @Nonnull File file) {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            parser.parse(in, createHandlerFor(analysisContext));
        } catch (StopParsing command) {
            // just do nothing
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse [" + file + "]!", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Used to indicate that XML parsing can be stopped.
     *
     * @since 1.2.0
     */
    protected static final class StopParsing extends SAXException {
    }

}
