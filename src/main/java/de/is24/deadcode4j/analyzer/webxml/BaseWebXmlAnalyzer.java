package de.is24.deadcode4j.analyzer.webxml;

import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.XmlAnalyzer;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import java.util.*;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Iterables.elementsEqual;
import static java.util.Arrays.asList;

/**
 * Parses {@code web.xml} and translates XML events into {@code web.xml}
 * specific events that can be consumed by a {@link WebXmlHandler}. It only
 * creates events for {@code web.xml} nodes that are needed by existing
 * {@link de.is24.deadcode4j.Analyzer}s. Please add further events if needed.
 *
 * @since 2.1.0
 */
public abstract class BaseWebXmlAnalyzer extends XmlAnalyzer {
    protected BaseWebXmlAnalyzer() {
        super("web.xml");
    }

    /**
     * This method is called to provide a <code>WebXmlHandler</code> for each file being processed.
     */
    @Nonnull
    protected abstract WebXmlHandler createWebXmlHandlerFor(@Nonnull AnalysisContext analysisContext);

    @Nonnull
    @Override
    protected DefaultHandler createHandlerFor(@Nonnull AnalysisContext analysisContext) {
        WebXmlHandler webXmlHandler = createWebXmlHandlerFor(analysisContext);
        return new WebXmlAdapter(webXmlHandler);
    }

    // Translates XML events into web.xml events that can be consumed by a WebXmlHandler
    private static class WebXmlAdapter extends DefaultHandler {
        @SuppressWarnings("unchecked")
        static final Collection<List<String>> NODES_WITH_TEXT = asList(
                asList("web-app", "context-param", "param-name"),
                asList("web-app", "context-param", "param-value"),
                asList("web-app", "filter", "filter-class"),
                asList("web-app", "filter", "init-param", "param-name"),
                asList("web-app", "filter", "init-param", "param-value"),
                asList("web-app", "listener", "listener-class"),
                asList("web-app", "servlet", "servlet-class"),
                asList("web-app", "servlet", "init-param", "param-name"),
                asList("web-app", "servlet", "init-param", "param-value"));
        static final Collection<String> CONTEXT_PARAM_PATH = asList("web-app", "context-param");
        static final Collection<String> FILTER_PATH = asList("web-app", "filter");
        static final Collection<String> FILTER_INIT_PARAM_PATH = asList("web-app", "filter", "init-param");
        static final Collection<String> LISTENER_PATH = asList("web-app", "listener");
        static final Collection<String> SERVLET_INIT_PARAM_PATH = asList("web-app", "servlet", "init-param");
        static final Collection<String> SERVLET_PATH = asList("web-app", "servlet");

        final Deque<String> deque = new ArrayDeque<String>();
        final List<Param> initParams = new ArrayList<Param>();
        final Map<String, String> texts = new HashMap<String, String>();
        final Deque<StringBuilder> textBuffers = new ArrayDeque<StringBuilder>();
        final WebXmlHandler webXmlHandler;

        WebXmlAdapter(WebXmlHandler webXmlHandler) {
            this.webXmlHandler = webXmlHandler;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            deque.add(localName);
            if (isNodeWithText()) {
                textBuffers.addLast(new StringBuilder(128));
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (isNodeWithText()) {
                textBuffers.getLast().append(new String(ch, start, length).trim());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (isNodeWithText()) {
                storeCharacters(localName);
            } else if (matchesPath(CONTEXT_PARAM_PATH)) {
                reportContextParam();
            } else if (matchesPath(FILTER_INIT_PARAM_PATH)) {
                storeInitParam();
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

        boolean isNodeWithText() {
            for (List<String> candidate : NODES_WITH_TEXT) {
                if (matchesPath(candidate)) {
                    return true;
                }
            }
            return false;
        }

        void reportContextParam() {
            webXmlHandler.contextParam(createParam());
        }

        void storeInitParam() {
            initParams.add(createParam());
        }

        Param createParam() {
            return new Param(getText("param-name"), getText("param-value"));
        }

        void reportFilter() {
            webXmlHandler.filter(
                    getText("filter-class"),
                    new ArrayList<Param>(initParams));
            initParams.clear();
        }

        void reportListener() {
            webXmlHandler.listener(getText("listener-class"));
        }

        void reportServlet() {
            webXmlHandler.servlet(
                    getText("servlet-class"),
                    new ArrayList<Param>(initParams));
            initParams.clear();
        }

        boolean matchesPath(Collection<String> path) {
            return path.size() == deque.size() && elementsEqual(path, deque);
        }

        void storeCharacters(String localName) {
            texts.put(localName, textBuffers.removeLast().toString());
        }

        String getText(String localName) {
            String text = texts.remove(localName);
            return nullToEmpty(text);
        }
    }
}
