package de.is24.deadcode4j.analyzer.webxml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Iterables.elementsEqual;
import static java.util.Arrays.asList;


/**
 * Translates XML events into web.xml events that can be consumed by a
 * {@link WebXmlHandler}. It only creates events for web.xml nodes that are
 * needed by existing {@link de.is24.deadcode4j.Analyzer}s. Please add further
 * events if needed.
 *
 * @since 2.1.0
 */
public class WebXmlAdapter extends DefaultHandler {
    private static final Collection<List<String>> NODES_WITH_TEXT = asList(
            asList("web-app", "context-param", "param-name"),
            asList("web-app", "context-param", "param-value"),
            asList("web-app", "filter", "filter-class"),
            asList("web-app", "listener", "listener-class"),
            asList("web-app", "servlet", "servlet-class"),
            asList("web-app", "servlet", "init-param", "param-name"),
            asList("web-app", "servlet", "init-param", "param-value"));
    private static final Collection<String> CONTEXT_PARAM_PATH = asList("web-app", "context-param");
    private static final Collection<String> FILTER_PATH = asList("web-app", "filter");
    private static final Collection<String> LISTENER_PATH = asList("web-app", "listener");
    private static final Collection<String> SERVLET_INIT_PARAM_PATH = asList("web-app", "servlet", "init-param");
    private static final Collection<String> SERVLET_PATH = asList("web-app", "servlet");

    private final Deque<String> deque = new ArrayDeque<String>();
    private final List<Param> initParams = new ArrayList<Param>();
    private final Map<String, String> texts = new HashMap<String, String>();
    private StringBuilder buffer;
    private final WebXmlHandler webXmlHandler;

    public WebXmlAdapter(WebXmlHandler webXmlHandler) {
        this.webXmlHandler = webXmlHandler;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        deque.add(localName);
        if (isNodeWithText()) {
            buffer = new StringBuilder(128);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (isNodeWithText()) {
            buffer.append(new String(ch, start, length).trim());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (isNodeWithText()) {
            storeCharacters(localName);
        } else if (matchesPath(CONTEXT_PARAM_PATH)) {
            reportContextParam();
        } else if (matchesPath(FILTER_PATH)) {
            reportFilter();
        } else if (matchesPath(LISTENER_PATH)) {
            reportListener();
        } else if (matchesPath(SERVLET_INIT_PARAM_PATH)) {
            storeInitParam();
        } else if (matchesPath(SERVLET_PATH)) {
            reportServlet();
        }
        deque.removeLast();
    }

    private boolean isNodeWithText() {
        for (List<String> candidate : NODES_WITH_TEXT) {
            if (matchesPath(candidate)) {
                return true;
            }
        }
        return false;
    }

    private void reportContextParam() {
        webXmlHandler.contextParam(createParam());
    }

    private void storeInitParam() {
        initParams.add(createParam());
    }

    private Param createParam() {
        return new Param(getText("param-name"), getText("param-value"));
    }

    private void reportFilter() {
        webXmlHandler.filter(getText("filter-class"));
    }

    private void reportListener() {
        webXmlHandler.listener(getText("listener-class"));
    }

    private void reportServlet() {
        webXmlHandler.servlet(
                getText("servlet-class"),
                new ArrayList<Param>(initParams));
        initParams.clear();
    }

    private boolean matchesPath(Collection<String> path) {
        return path.size() == deque.size() && elementsEqual(path, deque);
    }

    private void storeCharacters(String localName) {
        texts.put(localName, buffer.toString());
    }

    private String getText(String localName) {
        String text = texts.remove(localName);
        return nullToEmpty(text);
    }
}
