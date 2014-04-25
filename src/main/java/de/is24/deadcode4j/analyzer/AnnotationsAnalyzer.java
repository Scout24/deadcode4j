package de.is24.deadcode4j.analyzer;

import com.google.common.base.Function;
import de.is24.deadcode4j.CodeContext;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.annotation.Annotation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Inherited;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.util.Collections.disjoint;
import static java.util.Collections.emptySet;

/**
 * Serves as a base class with which to mark classes as being in use if they carry one of the specified annotations.
 *
 * @since 1.3
 */
public abstract class AnnotationsAnalyzer extends ByteCodeAnalyzer {
    private static final Set<String> DEAD_ENDS = newHashSet(
            "java.lang.annotation.Documented",
            "java.lang.annotation.Inherited",
            "java.lang.annotation.Retention",
            "java.lang.annotation.Target");
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
            allAnnotations.addAll(getInheritedAnnotations(codeContext, clazz));
        } catch (NotFoundException e) {
            logger.warn("The class path is not correctly set up! Skipping interfaces check for {}.", clazz.getName(), e);
            return;
        }

        if (!disjoint(this.annotations, allAnnotations)) {
            codeContext.addDependencies(this.dependerId, className);
        }
    }

    private void addAnnotations(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz, Set<String> knownAnnotations) throws NotFoundException {
        for (Annotation annotation : getAnnotations(clazz, PACKAGE, TYPE)) {
            String annotationClassName = annotation.getTypeName();
            if (!knownAnnotations.add(annotationClassName))
                continue;
            if (DEAD_ENDS.contains(annotationClassName))
                continue;
            ClassPool classPool = getOrCreateClassPool(codeContext);
            CtClass annotationClazz = classPool.get(annotationClassName);
            addAnnotations(codeContext, annotationClazz, knownAnnotations);
        }
    }

    @Nonnull
    private Set<String> getInheritedAnnotations(@Nonnull final CodeContext codeContext, @Nonnull final CtClass clazz) throws NotFoundException {
        List<String> annotationsMarkedAsInherited = getOrLookUpAnnotationsMarkedAsInherited(codeContext);
        if (annotationsMarkedAsInherited.isEmpty()) {
            return emptySet();
        }
        final Set<String> inheritedAnnotations = newHashSet();
        getClassHierarchy(clazz, new Function<CtClass, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable CtClass clazz) {
                if (clazz != null) {
                    for (Annotation annotation : getAnnotations(clazz, PACKAGE, TYPE)) {
                        String annotationClassName = annotation.getTypeName();
                        inheritedAnnotations.add(annotationClassName);
                    }
                }
                return null;
            }
        });
        return inheritedAnnotations;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private List<String> getOrLookUpAnnotationsMarkedAsInherited(@Nonnull CodeContext codeContext) {
        List<String> inheritedAnnotations = (List<String>) codeContext.getCache().get(getClass());
        if (inheritedAnnotations == null) {
            inheritedAnnotations = computeAnnotationsMarkedAsInherited(codeContext);
            codeContext.getCache().put(getClass(), inheritedAnnotations);
        }
        return inheritedAnnotations;
    }

    @Nonnull
    private List<String> computeAnnotationsMarkedAsInherited(@Nonnull CodeContext codeContext) {
        List<String> inheritedAnnotations = newArrayList();
        ClassPool classPool = getOrCreateClassPool(codeContext);
        for (String annotation : annotations) {
            CtClass annotationClazz;
            try {
                annotationClazz = classPool.get(annotation);
            } catch (NotFoundException e) {
                logger.debug("Annotation [{}] cannot be found on the class path; skipping detection");
                continue;
            }
            try {
                if (annotationClazz.getAnnotation(Inherited.class) != null) {
                    inheritedAnnotations.add(annotation);
                }
            } catch (ClassNotFoundException e) {
                logger.debug("@Inherited is not available; we probably deal with Java < 5.");
            }
        }
        return inheritedAnnotations;
    }

}
