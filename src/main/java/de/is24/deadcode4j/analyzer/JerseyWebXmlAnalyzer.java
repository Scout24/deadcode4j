package de.is24.deadcode4j.analyzer;


import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.webxml.BaseWebXmlAnalyzer;
import de.is24.deadcode4j.analyzer.webxml.Param;
import de.is24.deadcode4j.analyzer.webxml.WebXmlHandler;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Analyzes web.xml files: looks for servlet parameters that define the JAX-RS applications.
 *
 * @since 2.1.0
 */
public class JerseyWebXmlAnalyzer extends BaseWebXmlAnalyzer {
    @Nonnull
    @Override
    protected WebXmlHandler createWebXmlHandlerFor(@Nonnull final AnalysisContext analysisContext) {
        return new WebXmlHandler() {
            @Override
            public void servlet(String className, List<Param> initParams) {
                for (Param initParam: initParams) {
                    if (initParam.getName().equals("javax.ws.rs.Application")) {
                        analysisContext.addDependencies("_Jersey-SpringServlet_", initParam.getValue());
                    }
                }
            }
        };
    }
}
