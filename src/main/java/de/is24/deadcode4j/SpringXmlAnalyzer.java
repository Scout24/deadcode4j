package de.is24.deadcode4j;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Analyzes Spring XML files: lists the classes being referenced.
 *
 * @since 1.0.2
 */
public class SpringXmlAnalyzer implements Analyzer {
    private ClassLoader classLoader;
    private final Collection<String> referencedClasses = newArrayList();

    @Nonnull
    public AnalyzedCode analyze(CodeContext codeContext) {
        this.classLoader = codeContext.getClassLoader();
        for (File codeRepository : codeContext.getCodeRepositories()) {
            analyzeRepository(codeRepository);
        }

        return new AnalyzedCode(Collections.<String>emptyList(), Collections.<String, Iterable<String>>singletonMap("SpringBeans", referencedClasses));
    }

    private void analyzeRepository(@Nonnull File codeRepository) {
        analyzeFile(codeRepository, codeRepository);

    }

    private void analyzeFile(@Nonnull File codeRepository, @Nonnull File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File childNode : children) {
                    analyzeFile(codeRepository, childNode);
                }
            }
            return;
        }
        String fileName = file.getAbsolutePath().substring(codeRepository.getAbsolutePath().length() + 1);
        if (fileName.endsWith(".xml")) {
            analyzeXmlFile(fileName);
        }
    }

    private void analyzeXmlFile(@Nonnull String file) {
        final SAXParser parser;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            parser = factory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up XML parser!", e);
        }

        try {
            parser.parse(this.classLoader.getResourceAsStream(file), new DefaultHandler() {
                private boolean firstElement = true;
                private boolean isSpringFile = false;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    if (firstElement && "beans".equals(qName)) {
                        isSpringFile = true;
                    } else {
                        firstElement = false;
                    }
                    if (!isSpringFile) {
                        return;
                    }
                    if (!"bean".equals(qName)) {
                        return;
                    }

                    final String className = attributes.getValue("class");
                    if (className != null) {
                        referencedClasses.add(className);
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML file!", e);
        }
    }

}
