package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;

/**
 * Analyzes class files: marks a class as being in use if it is annotated with one of the specified annotations.
 *
 * @since 1.3.0
 */
public class CustomAnnotationsAnalyzer extends ByteCodeAnalyzer {

    private final Collection<String> customAnnotations;

    /**
     * Creates a new <code>CustomAnnotationsAnalyzer</code>.
     *
     * @param customAnnotations a list of fully qualified (annotation) class names indicating a class is still in use
     * @since 1.3.0
     */
    public CustomAnnotationsAnalyzer(@Nonnull Iterable<String> customAnnotations) {
        this.customAnnotations = newHashSet(customAnnotations);
        if (this.customAnnotations.isEmpty()) {
            throw new IllegalArgumentException("customAnnotations cannot by empty!");
        }
    }

    @Override
    protected void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        String className = clazz.getName();
        codeContext.addAnalyzedClass(className);
        for (String annotation : getAnnotationsOf(clazz)) {
            if (this.customAnnotations.contains(annotation)) {
                codeContext.addDependencies("_custom-annotations_", singleton(className));
            }
        }
    }

    private Iterable<String> getAnnotationsOf(CtClass ctClass) {
        @SuppressWarnings("unchecked") List<AttributeInfo> attributes = ctClass.getClassFile2().getAttributes();

        ArrayList<String> annotations = new ArrayList<String>();
        for (AttributeInfo attribute : attributes) {
            if (!AnnotationsAttribute.class.isInstance(attribute)) {
                continue;
            }
            for (Annotation annotation : AnnotationsAttribute.class.cast(attribute).getAnnotations()) {
                annotations.add(annotation.getTypeName());
            }
        }


        return annotations;
    }

}
