package de.is24.deadcode4j;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Analyzes Spring XML files: lists the classes being referenced.
 *
 * @since 1.1.0
 */
public class SpringXmlAnalyzer extends XmlAnalyzer implements Analyzer {
    private final XmlHandler handler;
    private final Collection<String> referencedClasses = newArrayList();

    public SpringXmlAnalyzer() {
        this.handler = new XmlHandler();
    }

    @Override
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull String fileName) {
        if (fileName.endsWith(".xml")) {
            analyzeXmlFile(codeContext, fileName);
        }
    }

    private void analyzeXmlFile(@Nonnull CodeContext codeContext, @Nonnull String file) {
        this.referencedClasses.clear();
        this.handler.reset();
        try {
            parser.parse(codeContext.getClassLoader().getResourceAsStream(file), handler);
        } catch (StopParsing command) {
            return;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse [" + file + "]!", e);
        }
        codeContext.addDependencies("_Spring_", this.referencedClasses);
    }

    private class XmlHandler extends DefaultHandler {
        private boolean firstElement = true;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws StopParsing {
            if (firstElement && !"beans".equals(localName)) {
                throw new StopParsing();
            } else {
                firstElement = false;
            }
            if (!"bean".equals(localName)) {
                return;
            }

            String className = attributes.getValue("class");
            if (className != null) {
                referencedClasses.add(className);
            }
        }

        public void reset() {
            this.firstElement = true;
        }

    }

}
