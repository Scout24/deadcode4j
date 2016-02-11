package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JMockit.class)
public class An_XmlAnalyzer extends AnAnalyzer<XmlAnalyzer> {

    private static final String XML_FILE = "de/is24/deadcode4j/analyzer/empty.xml";

    private AtomicBoolean fileIsParsed;

    @Override
    protected XmlAnalyzer createAnalyzer() {
        fileIsParsed = new AtomicBoolean(false);

        return new XmlAnalyzer(".xml") {
            @Nonnull
            @Override
            protected DefaultHandler createHandlerFor(@Nonnull AnalysisContext analysisContext) {
                return new DefaultHandler() {
                    @Override
                    public void startDocument() throws SAXException {
                        fileIsParsed.set(true);
                    }
                };
            }
        };
    }

    @Test
    public void parsesMatchingFile() {
        analyzeFile(XML_FILE);

        assertTrue("Should have analyzed the XML file!", fileIsParsed.get());
    }

    @Test
    public void doesNotParseNonMatchingFile() {
        objectUnderTest = new XmlAnalyzer(".foo") {
            @Nonnull
            @Override
            protected DefaultHandler createHandlerFor(@Nonnull AnalysisContext analysisContext) {
                Assert.fail("Should NOT have analyzed the XML file!");
                return new DefaultHandler();
            }
        };

        analyzeFile(XML_FILE);
    }

    @Test
    public void handlesIOExceptionWhenAnalyzingFile() {
        new MockUp<SAXParser>() {
            @Mock
            public void parse(InputStream is, DefaultHandler dh) throws SAXException, IOException {
                throw new IOException("JUnit");
            }
        };

        try {
            analyzeFile(XML_FILE);
            fail("Should abort analysis!");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString(XML_FILE));
        }
    }

    @Test(expected = RuntimeException.class)
    public void handlesSaxExceptionInConstructor() {
        new MockUp<SAXParserFactory>() {
            @Mock
            public SAXParserFactory newInstance() {
                return new SAXParserFactory() {
                    @Override
                    public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
                        throw new SAXException("JUnit");
                    }
                    @Override
                    public void setFeature(String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException { }
                    @Override
                    public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
                        return false;
                    }
                };
            }
        };

        createAnalyzer();
        fail("Should not be able to construct XmlAnalyzer!");
    }

}
