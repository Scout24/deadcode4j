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
 * <p>A user can extend the Jersey Application class. In order to use this application the user must set the init-param
 * "javax.ws.rs.Application" of a servlet or a filter. (We don't check the servlet or filter class because there are
 * several classes of this kind. See Jersey documentation
 * <a href="https://jersey.java.net/documentation/latest/deployment.html#deployment.servlet.2">4.7.1.1. Custom
 * Application subclass</a>
 *
 * @since 2.1.0
 */
public class JerseyWebXmlAnalyzer extends BaseWebXmlAnalyzer {
    @Nonnull
    @Override
    protected WebXmlHandler createWebXmlHandlerFor(@Nonnull final AnalysisContext analysisContext) {
        return new WebXmlHandler() {
            @Override
            public void filter(String className, List<Param> initParams) {
                //We don't check the filter class because there are several classes of this kind.
                scanParams(initParams);
            }

            @Override
            public void servlet(String className, List<Param> initParams) {
                //We don't check the servlet class because there are several classes of this kind.
                scanParams(initParams);
            }

            private void scanParams(List<Param> initParams) {
                for (Param initParam: initParams) {
                    if (initParam.getName().equals("javax.ws.rs.Application")) {
                        analysisContext.addDependencies("_Jersey_", initParam.getValue());
                    }
                }
            }
        };
    }
}
