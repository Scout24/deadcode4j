package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;

/**
 * Analyzes class files: marks a class as being in use if it is annotated with
 * <code>javax.faces.convert.FacesConverter</code>.
 *
 * @since 1.4
 */
public final class JsfAnnotationsAnalyzer extends AnnotationsAnalyzer implements Analyzer {

    public JsfAnnotationsAnalyzer() {
        super("_JSF-Annotation_", "javax.faces.convert.FacesConverter");
    }

}
