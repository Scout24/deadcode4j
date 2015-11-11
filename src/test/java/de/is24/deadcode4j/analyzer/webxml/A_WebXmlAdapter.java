package de.is24.deadcode4j.analyzer.webxml;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class A_WebXmlAdapter {
    @Test
    public void sendsContextParamEventForAContextParamNode() throws Exception {
        WebXmlHandler handler = mock(WebXmlHandler.class);
        parseFileWithHandler("web.xml", handler);
        verify(handler).contextParam(
                eq(new Param("dummy name", "dummy value")));
    }

    @Test
    public void sendsFilterEventForAFilterNode() throws Exception {
        WebXmlHandler handler = mock(WebXmlHandler.class);
        parseFileWithHandler("web.xml", handler);
        verify(handler).filter("dummy.filter.class");
    }

    @Test
    public void sendsListenerEventForAListenerNode() throws Exception {
        WebXmlHandler handler = mock(WebXmlHandler.class);
        parseFileWithHandler("web.xml", handler);
        verify(handler).listener("dummy.listener.class");
    }

    @Test
    public void sendsServletEventForAServletNode() throws Exception {
        WebXmlHandler handler = mock(WebXmlHandler.class);
        parseFileWithHandler("web.xml", handler);
        verify(handler).servlet(
                eq("dummy.servlet.class"),
                eq(asList(new Param("dummy name", "dummy value"))));
    }

    private void parseFileWithHandler(String resourceName, WebXmlHandler handler)
            throws ParserConfigurationException, SAXException, IOException {
        InputStream resource = getClass().getResourceAsStream(resourceName);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/namespaces", true);
        SAXParser parser = factory.newSAXParser();
        parser.parse(resource, new WebXmlAdapter(handler));
    }
}