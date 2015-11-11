package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.webxml.Param;
import de.is24.deadcode4j.analyzer.webxml.WebXmlAdapter;
import de.is24.deadcode4j.analyzer.webxml.WebXmlHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.elementsEqual;
import static com.google.common.collect.Iterables.paddedPartition;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static java.util.Arrays.asList;


/**
 * Analyzes web.xml files: looks for context parameters or servlet init parameters
 * <ul>
 * <li>named <i>contextClass</i> referring to an instance of <tt><a href="http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/context/ConfigurableWebApplicationContext.html">ConfigurableWebApplicationContext</a></tt> or</li>
 * <li>named <i>contextInitializerClasses</i> referring to several instances of <tt><a href="http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/context/ApplicationContextInitializer.html">ApplicationContextInitializer</a></tt></li>
 * </ul>.
 *
 * @since 1.4
 */
public final class SpringWebXmlAnalyzer extends XmlAnalyzer {
    public SpringWebXmlAnalyzer() {
        super("web.xml");
    }

    @Nonnull
    @Override
    protected DefaultHandler createHandlerFor(@Nonnull final AnalysisContext analysisContext) {
        return new WebXmlAdapter(new WebXmlHandler() {
            @Override
            public void contextParam(Param param) {
                param(param);
            }

            @Override
            public void servlet(String className, List<Param> initParams) {
                for (Param initParam : initParams) {
                    param(initParam);
                }
            }

            private void param(Param param) {
                if ("contextClass".equals(param.getName())) {
                    analysisContext.addDependencies("_Spring-Context_", param.getValue());
                } else if ("contextInitializerClasses".equals(param.getName())) {
                    for (String initializerClass : param.getValue().split(",")) {
                        initializerClass = initializerClass.trim();
                        if (!initializerClass.isEmpty()) {
                            analysisContext.addDependencies("_Spring-ContextInitializer_", initializerClass);
                        }
                    }
                } else if ("contextConfigLocation".equals(param.getName())) {
                    for (String configLocation : param.getValue().split(",")) {
                        configLocation = configLocation.trim();
                        if (!configLocation.isEmpty()) {
                            Optional<String> referencedClass =
                                    classPoolAccessorFor(analysisContext).resolveClass(configLocation);
                            if (referencedClass.isPresent()) {
                                analysisContext.addDependencies("_Spring-ContextInitializer_", referencedClass.get());
                            }
                        }
                    }
                }
            }
        });
    }
}
