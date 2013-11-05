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
        super("_JSF-Annotation_",
                "javax.faces.component.behavior.FacesBehavior",
                "javax.faces.convert.FacesConverter",
                "javax.faces.event.ListenerFor",
                "javax.faces.event.ListenersFor",
                "javax.faces.event.NamedEvent",
                "javax.faces.render.FacesBehaviorRenderer",
                "javax.faces.render.FacesRenderer",
                "javax.faces.validator.FacesValidator",
                "javax.faces.view.facelets.FaceletsResourceResolver");
    }

}
