package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import java.io.File;

import static com.google.common.collect.Iterables.concat;

/**
 * Analyzes both <code>web.xml</code> and class files: looks for implementations of
 * {@link javax.servlet.ServletContainerInitializer} if the <code>metadata-complete</code> attribute of the
 * <code>web-app</code> element is missing or set to "false".
 *
 * @since 1.5
 */
public class ServletContainerInitializerAnalyzer implements Analyzer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ServletContainerInitializerCodeContext context = new ServletContainerInitializerCodeContext();
    private final Analyzer classFinder;
    private final String depender;

    /**
     * Creates a new instance of <code>ServletContainerInitializerAnalyzer</code>.
     *
     * @param dependerId                 a description of the <i>depending entity</i> with which to
     *                                   call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, Iterable)}
     * @param fqdnOfInitializerInterface the fqdn of the interface whose implementations represent a
     *                                   <code>ServletContainerInitializer</code> or something comparable
     */
    protected ServletContainerInitializerAnalyzer(String dependerId, String fqdnOfInitializerInterface) {
        this.depender = dependerId;
        this.classFinder = new InterfacesAnalyzer("ServletContainerInitializer-implementation", fqdnOfInitializerInterface) {
        };
    }

    public ServletContainerInitializerAnalyzer() {
        this("JEE-ServletContainerInitializer", "javax.servlet.ServletContainerInitializer");
    }

    private final Analyzer webXmlAnalyzer = new XmlAnalyzer("web.xml") {
        @Nonnull
        @Override
        protected DefaultHandler createHandlerFor(@Nonnull final CodeContext codeContext) {
            return new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws StopParsing {
                    if ("web-app".equals(localName) && "true".equals(attributes.getValue("metadata-complete"))) {
                        ((ServletContainerInitializerCodeContext) codeContext).setMetadataComplete();
                    }
                    throw new StopParsing();
                }
            };
        }
    };

    @Override
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull File fileName) {
        this.context.setOriginalContext(codeContext);
        this.webXmlAnalyzer.doAnalysis(this.context, fileName);
        this.classFinder.doAnalysis(this.context, fileName);
    }

    @Override
    public void finishAnalysis(@Nonnull CodeContext codeContext) {
        if (this.context.isMetadataComplete()) {
            logger.debug("Found web.xml with completed metadata; " +
                    "ServletContainerInitializer implementations are treated as dead code");
            return;
        }
        Iterable<String> initializerClasses = concat(this.context.getAnalyzedCode().getCodeDependencies().values());
        if (initializerClasses != null) {
            codeContext.addDependencies(depender, initializerClasses);
        }
    }

    private static class ServletContainerInitializerCodeContext extends CodeContext {

        private CodeContext originalContext;
        private boolean metadataComplete = false;

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

        public void setOriginalContext(CodeContext codeContext) {
            this.originalContext = codeContext;
        }

    }

}
