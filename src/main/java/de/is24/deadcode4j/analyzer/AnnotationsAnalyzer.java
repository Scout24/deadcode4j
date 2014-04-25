package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.annotation.Annotation;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.util.Collections.disjoint;

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
     *                    call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, Iterable)}
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
     *                    call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, Iterable)}
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

        Set<String> allAnnotations = newHashSet();
        try {
            addAnnotations(codeContext, clazz, allAnnotations);
        } catch (ClassNotFoundException e) {
            logger.warn("The class path is not correctly set up! Skipping interfaces check for {}.", clazz.getName(), e);
            return;
        } catch (NotFoundException e) {
            logger.warn("The class path is not correctly set up! Skipping interfaces check for {}.", clazz.getName(), e);
            return;
        }

        if (!disjoint(this.annotations, allAnnotations)) {
            codeContext.addDependencies(this.dependerId, className);
        }
    }

    private void addAnnotations(CodeContext codeContext, CtClass clazz, Set<String> knownAnnotations) throws ClassNotFoundException, NotFoundException {
        for (Annotation annotation : getAnnotations(clazz, PACKAGE, TYPE)) {
            String annotationClassName = annotation.getTypeName();
            if (!knownAnnotations.add(annotationClassName))
                continue;
            ClassPool classPool = getOrCreateClassPool(codeContext);
            CtClass annotationClazz = classPool.get(annotationClassName);
            addAnnotations(codeContext, annotationClazz, knownAnnotations);
        }
    }

}
