package de.is24.deadcode4j.analyzer;

/**
 * Analyzes both <code>web.xml</code> and class files: looks for implementations of
 * {@link org.springframework.web.WebApplicationInitializer} if the <code>metadata-complete</code> attribute of the
 * <code>web-app</code> element is missing or set to "false".
 *
 * @since 1.5
 */
public class SpringWebApplicationInitializerAnalyzer extends ServletContainerInitializerAnalyzer {

    public SpringWebApplicationInitializerAnalyzer() {
        super("_Spring-WebApplicationInitializer_", "org.springframework.web.WebApplicationInitializer");
    }

}
