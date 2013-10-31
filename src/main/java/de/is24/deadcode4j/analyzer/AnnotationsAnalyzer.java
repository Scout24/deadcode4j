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
 * Serves as a base class with which to mark classes as being in use if they carry one of the specified annotations.
 *
 * @since 1.3
 */
public abstract class AnnotationsAnalyzer extends ByteCodeAnalyzer {

    private final Collection<String> annotations;
    private final String dependerId;

    /**
     * Creates a new <code>AnnotationsAnalyzer</code>.
     *
     * @param dependerId  a description of the <i>depending entity</i> with which to
     *                    call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, java.util.Collection)}
     * @param annotations a list of fully qualified (annotation) class names indicating a class is still in use
     * @since 1.3
     */
    protected AnnotationsAnalyzer(@Nonnull String dependerId, @Nonnull Iterable<String> annotations) {
        this.dependerId = dependerId;
        this.annotations = newHashSet(annotations);
        if (this.annotations.isEmpty()) {
            throw new IllegalArgumentException("annotations cannot by empty!");
        }
    }

    @Override
    protected final void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        String className = clazz.getName();
        codeContext.addAnalyzedClass(className);
        for (String annotation : getAnnotationsOf(clazz)) {
            if (this.annotations.contains(annotation)) {
                codeContext.addDependencies(this.dependerId, singleton(className));
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
