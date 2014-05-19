package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.collect.Iterables.filter;

/**
 * Analyzes <a href="http://docs.spring.io/spring/docs/3.2.x/spring-framework-reference/html/extensible-xml.html">
 * <code>spring.handlers</code> property files</a> and lists the defined <i>namespace handlers</i> as classes being
 * referenced.
 */
public class SpringNamespaceHandlerAnalyzer extends AnalyzerAdapter {

    @Override
    public void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File file) {
        if (file.getAbsolutePath().endsWith("META-INF/spring.handlers")) {
            logger.debug("Analyzing property file [{}]...", file);
            registerSpringHandlersDefinedIn(analysisContext, file);
        }
    }

    private void registerSpringHandlersDefinedIn(AnalysisContext analysisContext, File file) {
        Properties springNamespaceHandlers = readPropertyFile(file);
        analysisContext.addDependencies("_Spring-NamespaceHandler_", filter(springNamespaceHandlers.values(), String.class));
    }

    private Properties readPropertyFile(File file) {
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read [" + file + "]!", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return properties;
    }


}
