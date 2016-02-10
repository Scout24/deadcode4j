package de.is24.deadcode4j.analyzer;

import com.google.common.collect.Maps;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.IntermediateResult;
import de.is24.deadcode4j.Module;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.isEmpty;

/**
 * Analyzes both <code>web.xml</code> and class files: looks for implementations of
 * {@link javax.servlet.ServletContainerInitializer} if the <code>metadata-complete</code> attribute of the
 * <code>web-app</code> element is missing or set to "false".
 *
 * @since 1.5
 */
public class ServletContainerInitializerAnalyzer extends AnalyzerAdapter {
    private final String depender;
    private final Analyzer classFinder;
    private final Analyzer webXmlAnalyzer = new XmlAnalyzer("web.xml") {
        @Nonnull
        @Override
        protected DefaultHandler createHandlerFor(@Nonnull final AnalysisContext analysisContext) {
            return new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws StopParsing {
                    if ("web-app".equals(localName) && "true".equals(attributes.getValue("metadata-complete"))) {
                        ((ServletContainerInitializerAnalysisContext) analysisContext).setMetadataComplete();
                    }
                    throw new StopParsing();
                }
            };
        }
    };
    private ServletContainerInitializerAnalysisContext context;

    /**
     * Creates a new instance of <code>ServletContainerInitializerAnalyzer</code>.
     *
     * @param dependerId                 a description of the <i>depending entity</i> with which to
     *                                   call {@link de.is24.deadcode4j.AnalysisContext#addDependencies(String, Iterable)}
     * @param fqcnOfInitializerInterface the fqcn of the interface whose implementations represent a
     *                                   <code>ServletContainerInitializer</code> or something comparable
     */
    protected ServletContainerInitializerAnalyzer(String dependerId, String fqcnOfInitializerInterface) {
        this.depender = dependerId;
        this.classFinder = new InterfacesAnalyzer("ServletContainerInitializer-implementation", fqcnOfInitializerInterface) {
        };
    }

    public ServletContainerInitializerAnalyzer() {
        this("JEE-ServletContainerInitializer", "javax.servlet.ServletContainerInitializer");
    }

    @Override
    public void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File fileName) {
        if (this.context == null) {
            this.context = new ServletContainerInitializerAnalysisContext(analysisContext.getModule());
            this.context.setOriginalContext(analysisContext);
        }
        this.webXmlAnalyzer.doAnalysis(this.context, fileName);
        this.classFinder.doAnalysis(this.context, fileName);
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisContext analysisContext) {
        ServletContainerInitializerAnalysisContext localContext = this.context;
        this.context = null;
        if (localContext == null) {
            return;
        }
        if (localContext.isMetadataComplete()) {
            logger.debug("Found web.xml with completed metadata; " +
                    "ServletContainerInitializer implementations are treated as dead code");
            return;
        }
        Iterable<String> initializerClasses = concat(localContext.getAnalyzedCode().getCodeDependencies().values());
        if (!isEmpty(initializerClasses)) {
            analysisContext.addDependencies(depender, initializerClasses);
        }
    }

    private static class ServletContainerInitializerAnalysisContext extends AnalysisContext {

        private AnalysisContext originalContext;
        private boolean metadataComplete = false;

        ServletContainerInitializerAnalysisContext(Module module) {
            super(module, Maps.<Object, IntermediateResult>newHashMap());
        }

        @Nonnull
        @Override
        public Map<Object, Object> getCache() {
            return this.originalContext.getCache();
        }

        @Nullable
        @Override
        public IntermediateResult getIntermediateResult(@Nonnull Object key) {
            return this.originalContext.getIntermediateResult(key);
        }

        @Override
        public void addAnalyzedClass(@Nonnull String clazz) {
            this.originalContext.addAnalyzedClass(clazz);
        }

        public void setMetadataComplete() {
            this.metadataComplete = true;
        }

        public boolean isMetadataComplete() {
            return metadataComplete;
        }

        public void setOriginalContext(AnalysisContext analysisContext) {
            this.originalContext = analysisContext;
        }

    }

}
