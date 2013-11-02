package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;

/**
 * Analyzes class files: marks a class as being in use if it is annotated with
 * <code>javax.xml.bind.annotation.XmlSchema</code>.
 *
 * @since 1.4
 */
public final class JaxbAnnotationsAnalyzer extends AnnotationsAnalyzer implements Analyzer {

    public JaxbAnnotationsAnalyzer() {
        super("_JAXB-Annotation_", "javax.xml.bind.annotation.XmlSchema");
    }

}
