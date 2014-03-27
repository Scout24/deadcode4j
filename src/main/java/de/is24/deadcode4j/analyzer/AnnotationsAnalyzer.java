package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import javassist.CtClass;
import javassist.bytecode.annotation.Annotation;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Serves as a base class with which to mark classes as being in use if they carry one of the specified annotations.
 *
 * @since 1.3
 */
public abstract class AnnotationsAnalyzer extends ByteCodeAnalyzer {

    private final Collection<String> annotations;
    private final String dependerId;

    private AnnotationsAnalyzer(@Nonnull String dependerId, @Nonnull Collection<String> annotations) {
        this.dependerId = dependerId;
        this.annotations = annotations;
        checkArgument(!this.annotations.isEmpty(), "annotations cannot by empty!");
    }

    /**
     * Creates a new <code>AnnotationsAnalyzer</code>.
     *
     * @param dependerId  a description of the <i>depending entity</i> with which to
     *                    call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, java.util.Collection)}
     * @param annotations a list of fully qualified (annotation) class names indicating a class is still in use
     * @since 1.3
     */
    protected AnnotationsAnalyzer(@Nonnull String dependerId, @Nonnull Iterable<String> annotations) {
        this(dependerId, newHashSet(annotations));
    }

    /**
     * Creates a new <code>AnnotationsAnalyzer</code>.
     *
     * @param dependerId  a description of the <i>depending entity</i> with which to
     *                    call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, java.util.Collection)}
     * @param annotations a list of fully qualified (annotation) class names indicating a class is still in use
     * @since 1.4
     */
    protected AnnotationsAnalyzer(@Nonnull String dependerId, @Nonnull String... annotations) {
        this(dependerId, newHashSet(annotations));
    }

    @Override
    protected final void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        String className = clazz.getName();
        codeContext.addAnalyzedClass(className);
        for (Annotation annotation : getAnnotations(clazz, PACKAGE, TYPE)) {
            if (this.annotations.contains(annotation.getTypeName()))
                codeContext.addDependencies(this.dependerId, className);
        }
    }

}
