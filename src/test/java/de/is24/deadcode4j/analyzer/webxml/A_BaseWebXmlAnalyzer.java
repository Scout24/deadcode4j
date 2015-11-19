package de.is24.deadcode4j.analyzer.webxml;

import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.AnAnalyzer;
import org.junit.Test;

import javax.annotation.Nonnull;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class A_BaseWebXmlAnalyzer extends AnAnalyzer<BaseWebXmlAnalyzer>{
    private final WebXmlHandler handler = mock(WebXmlHandler.class);

    @Override
    protected BaseWebXmlAnalyzer createAnalyzer() {
        return new BaseWebXmlAnalyzer() {
            @Nonnull
            @Override
            protected WebXmlHandler createWebXmlHandlerFor(@Nonnull AnalysisContext analysisContext) {
                return handler;
            }
        };
    }

    @Test
    public void sendsContextParamEventForAContextParamNode() throws Exception {
        analyzeFile("de/is24/deadcode4j/analyzer/webxml/web.xml");
        verify(handler).contextParam(
                eq(new Param("dummy name", "dummy value")));
    }

    @Test
    public void sendsFilterEventForAFilterNode() throws Exception {
        analyzeFile("de/is24/deadcode4j/analyzer/webxml/web.xml");
        verify(handler).filter(
                eq("dummy.filter.class"),
                eq(asList(new Param("dummy name", "dummy value"))));
    }

    @Test
    public void sendsListenerEventForAListenerNode() throws Exception {
        analyzeFile("de/is24/deadcode4j/analyzer/webxml/web.xml");
        verify(handler).listener("dummy.listener.class");
    }

    @Test
    public void sendsServletEventForAServletNode() throws Exception {
        analyzeFile("de/is24/deadcode4j/analyzer/webxml/web.xml");
        verify(handler).servlet(
                eq("dummy.servlet.class"),
                eq(asList(new Param("dummy name", "dummy value"))));
    }
}