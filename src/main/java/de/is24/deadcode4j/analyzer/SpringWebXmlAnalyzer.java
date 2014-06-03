package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import de.is24.deadcode4j.AnalysisContext;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.elementsEqual;
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
    private static final Collection<String> CONTEXT_PARAM_PATH = asList("web-app", "context-param");
    private static final Collection<String> SERVLET_INIT_PARAM_PATH = asList("web-app", "servlet", "init-param");

    public SpringWebXmlAnalyzer() {
        super("web.xml");
    }

    @Nonnull
    @Override
    protected DefaultHandler createHandlerFor(@Nonnull final AnalysisContext analysisContext) {
        return new DefaultHandler() {
            private final Deque<String> deque = new ArrayDeque<String>();
            private StringBuilder buffer;
            public String paramName;
            public String paramValue;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws StopParsing {
                if (isAtParameterLevel()) {
                    buffer = new StringBuilder(128);
                }
                deque.add(localName);
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                if (buffer != null) {
                    buffer.append(new String(ch, start, length).trim());
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if (isAtParameterLevel()) {
                    reportDependencies();
                    clearParameter();
                } else {
                    storeCharacters(localName);
                }
                deque.removeLast();
            }

            private boolean isAtParameterLevel() {
                return matchesPath(CONTEXT_PARAM_PATH) || matchesPath(SERVLET_INIT_PARAM_PATH);
            }

            private boolean matchesPath(Collection<String> path) {
                return path.size() == deque.size() && elementsEqual(path, deque);
            }

            private void reportDependencies() {
                if (paramValue == null) {
                    return;
                }
                if ("contextClass".equals(paramName)) {
                    analysisContext.addDependencies("_Spring-Context_", paramValue);
                } else if ("contextInitializerClasses".equals(paramName)) {
                    for (String initializerClass : paramValue.split(",")) {
                        initializerClass=initializerClass.trim();
                        if (!isNullOrEmpty(initializerClass)) {
                            analysisContext.addDependencies("_Spring-ContextInitializer_", initializerClass);
                        }
                    }
                } else if ("contextConfigLocation".equals(paramName)) {
                    for (String configLocation : paramValue.split(",")) {
                        configLocation=configLocation.trim();
                        if (isNullOrEmpty(configLocation)) {
                            continue;
                        }
                        Optional<String> referencedClass =
                                classPoolAccessorFor(analysisContext).resolveClass(configLocation);
                        if (referencedClass.isPresent()) {
                            analysisContext.addDependencies("_Spring-ContextInitializer_", referencedClass.get());
                        }
                    }
                }
            }

            private void clearParameter() {
                paramName = null;
                paramValue = null;
            }

            private void storeCharacters(String localName) {
                if (buffer != null) {
                    if ("param-name".equals(localName)) {
                        paramName = buffer.toString();
                    } else if ("param-value".equals(localName)) {
                        paramValue = buffer.toString();
                    }
                    buffer = null;
                }
            }

        };

    }
}
