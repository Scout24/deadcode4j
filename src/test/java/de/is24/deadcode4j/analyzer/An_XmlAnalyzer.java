package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(SAXParserFactory.class)
@RunWith(PowerMockRunner.class)
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
    public void handlesIOExceptionWhenAnalyzingFile() throws Exception {
        SAXParser saxMock = mock(SAXParser.class);
        doThrow(new IOException("JUnit")).when(saxMock).parse(Mockito.any(InputStream.class), Mockito.any(DefaultHandler.class));
        SAXParserFactory saxFactoryMock = mock(SAXParserFactory.class);
        when(saxFactoryMock.newSAXParser()).thenReturn(saxMock);
        PowerMockito.mockStatic(SAXParserFactory.class);
        when(SAXParserFactory.newInstance()).thenReturn(saxFactoryMock);

        initAnalyzer();

        try {
            analyzeFile(XML_FILE);
            fail("Should abort analysis!");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString(XML_FILE));
        }
    }

    @Test(expected = RuntimeException.class)
    public void handlesSaxExceptionInConstructor() throws ParserConfigurationException, SAXException {
        SAXParserFactory saxFactoryMock = mock(SAXParserFactory.class);
        when(saxFactoryMock.newSAXParser()).thenThrow(new SAXException("JUnit"));
        PowerMockito.mockStatic(SAXParserFactory.class);
        when(SAXParserFactory.newInstance()).thenReturn(saxFactoryMock);

        initAnalyzer();

        fail("Should not be able to construct XmlAnalyzer!");
    }

}
