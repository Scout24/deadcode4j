package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.webxml.BaseWebXmlAnalyzer;
import de.is24.deadcode4j.analyzer.webxml.Param;
import de.is24.deadcode4j.analyzer.webxml.WebXmlHandler;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Analyzes <code>web.xml</code> files: lists the listener, filter & servlet classes being referenced.
 *
 * @since 1.2.0
 */
public final class WebXmlAnalyzer extends BaseWebXmlAnalyzer {

    @Nonnull
    @Override
    protected WebXmlHandler createWebXmlHandlerFor(@Nonnull final AnalysisContext analysisContext) {
        return new WebXmlHandler() {
            @Override
            public void filter(String className, List<Param> initParams) {
                addDependency(className);
            }

            @Override
            public void listener(String className) {
                addDependency(className);
            }

            @Override
            public void servlet(String className, List<Param> initParams) {
                addDependency(className);
            }

            private void addDependency(String className) {
                analysisContext.addDependencies("_web.xml_", className);
            }
        };
    }
}
