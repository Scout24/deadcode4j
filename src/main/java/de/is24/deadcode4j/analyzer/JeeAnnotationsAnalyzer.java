package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;

import java.util.Arrays;

/**
 * Analyzes class files: marks a class as being in use if it is annotated with one of those JEE annotations:
 * <ul>
 * <li>javax.annotation.ManagedBean</li>
 * <li>javax.inject.Named</li>
 * <li>javax.persistence.metamodel.StaticMetamodel</li>
 * </ul>
 *
 * @since 1.3
 */
public final class JeeAnnotationsAnalyzer extends AnnotationsAnalyzer implements Analyzer {

    public JeeAnnotationsAnalyzer() {
        super("_JEE-Annotation_", Arrays.asList(
                "javax.annotation.ManagedBean",
                "javax.inject.Named",
                "javax.persistence.metamodel.StaticMetamodel"));
    }

}
