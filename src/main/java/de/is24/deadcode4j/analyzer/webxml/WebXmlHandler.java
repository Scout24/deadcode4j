package de.is24.deadcode4j.analyzer.webxml;

import java.util.List;

/**
 * Base class for web.xml event handlers. {@code WebXmlHandler}s are used by
 * {@link BaseWebXmlAnalyzer} that translates general XML events into web.xml
 * events.
 *
 * @since 2.1.0
 */
public abstract class WebXmlHandler {
    /**
     * Receive notification about a {@code context-param} node.
     *
     * @param param the param.
     */
    public void contextParam(Param param) {
    }

    /**
     * Receive notification about a {@code filter} node.
     *
     * @param className the text of the {@code filter-class} node.
     * @param initParams the filters init params.
     */
    public void filter(String className, List<Param> initParams) {
    }

    /**
     * Receive notification about a {@code listener} node.
     *
     * @param className the text of the {@code listener-class} node.
     */
    public void listener(String className) {
    }

    /**
     * Receive notification about a {@code servlet} node.
     *
     * @param className the text of the {@code servlet-class} node.
     * @param initParams the servlets init params.
     */
    public void servlet(String className, List<Param> initParams) {
    }
}
