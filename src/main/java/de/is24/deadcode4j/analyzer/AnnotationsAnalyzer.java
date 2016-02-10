package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPathFilter;
import de.is24.guava.NonNullFunction;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.annotation.Annotation;

import javax.annotation.Nonnull;
import java.lang.annotation.Inherited;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static de.is24.javassist.CtClasses.*;
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
    private final String dependerId;
    private final NonNullFunction<AnalysisContext, Set<String>> supplyAnnotationsFoundInClassPath;
    private final NonNullFunction<AnalysisContext, List<String>> supplyAnnotationsMarkedAsInherited = new NonNullFunction<AnalysisContext, List<String>>() {
        @Nonnull
        @Override
        public List<String> apply(@Nonnull AnalysisContext analysisContext) {
            List<String> inheritedAnnotations = newArrayList();
            ClassPool classPool = classPoolAccessorFor(analysisContext).getClassPool();
            for (String annotation : getAnnotationsFoundInClassPath(analysisContext)) {
                CtClass annotationClazz = classPool.getOrNull(annotation);
                if (annotationClazz == null) {
                    logger.debug("Annotation [{}] cannot be found on the class path; skipping detection", annotation);
                    continue;
                }
                try {
                    if (annotationClazz.getAnnotation(Inherited.class) != null) {
                        inheritedAnnotations.add(annotation);
                    }
                } catch (ClassNotFoundException e) {
                    logger.debug("@Inherited is not available; this is quite disturbing.");
                }
            }
            logger.debug("Found those inheritable annotations: {}", inheritedAnnotations);
            return inheritedAnnotations;
        }
    };

    private AnnotationsAnalyzer(@Nonnull String dependerId, @Nonnull Set<String> annotations) {
        checkArgument(!annotations.isEmpty(), "annotations cannot by empty!");
        this.dependerId = dependerId;
        this.supplyAnnotationsFoundInClassPath = new ClassPathFilter(annotations);
    }

    /**
     * Creates a new <code>AnnotationsAnalyzer</code>.
     *
     * @param dependerId  a description of the <i>depending entity</i> with which to
     *                    call {@link de.is24.deadcode4j.AnalysisContext#addDependencies(String, Iterable)}
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
     *                    call {@link de.is24.deadcode4j.AnalysisContext#addDependencies(String, Iterable)}
     * @param annotations a list of fully qualified (annotation) class names indicating a class is still in use
     * @since 1.4
     */
    protected AnnotationsAnalyzer(@Nonnull String dependerId, @Nonnull String... annotations) {
        this(dependerId, newHashSet(annotations));
    }

    @Override
    protected final void analyzeClass(@Nonnull AnalysisContext analysisContext, @Nonnull CtClass clazz) {
        Set<String> availableAnnotations = getAnnotationsFoundInClassPath(analysisContext);
        if (availableAnnotations.isEmpty()) {
            return;
        }

        String className = clazz.getName();
        analysisContext.addAnalyzedClass(className);

        Set<String> allAnnotations = newHashSet();
        addAnnotations(clazz, allAnnotations);
        allAnnotations.addAll(getInheritedAnnotations(analysisContext, clazz));

        if (!disjoint(availableAnnotations, allAnnotations)) {
            analysisContext.addDependencies(this.dependerId, className);
        }
    }

    private void addAnnotations(@Nonnull CtClass clazz, Set<String> knownAnnotations) {
        for (Annotation annotation : getAnnotations(clazz, PACKAGE, TYPE)) {
            String annotationClassName = annotation.getTypeName();
            if (!knownAnnotations.add(annotationClassName)) {
                continue;
            }
            if (DEAD_ENDS.contains(annotationClassName)) {
                continue;
            }
            CtClass annotationClazz = getCtClass(clazz.getClassPool(), annotationClassName);
            if (annotationClazz != null) {
                addAnnotations(annotationClazz, knownAnnotations);
            }
        }
    }

    @Nonnull
    private Set<String> getInheritedAnnotations(@Nonnull final AnalysisContext analysisContext, @Nonnull final CtClass clazz) {
        List<String> annotationsMarkedAsInherited = getAnnotationsMarkedAsInherited(analysisContext);
        if (annotationsMarkedAsInherited.isEmpty()) {
            return emptySet();
        }
        Set<String> inheritedAnnotations = newHashSet();
        CtClass loopClass = getSuperclassOf(clazz);
        while (loopClass != null && !isJavaLangObject(loopClass)) {
            for (Annotation annotation : getAnnotations(loopClass, PACKAGE, TYPE)) {
                inheritedAnnotations.add(annotation.getTypeName());
            }
            loopClass = getSuperclassOf(loopClass);
        }
        return inheritedAnnotations;
    }

    @Nonnull
    protected final Set<String> getAnnotationsFoundInClassPath(@Nonnull AnalysisContext analysisContext) {
        return analysisContext.getOrCreateCacheEntry(getClass().getName() + "|knownAnnotations", supplyAnnotationsFoundInClassPath);
    }

    @Nonnull
    private List<String> getAnnotationsMarkedAsInherited(@Nonnull AnalysisContext analysisContext) {
        return analysisContext.getOrCreateCacheEntry(getClass().getName() + "|inheritableAnnotations", supplyAnnotationsMarkedAsInherited);
    }

}
