package de.is24.deadcode4j.analyzer;

import com.google.common.collect.Sets;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPathFilter;
import de.is24.guava.NonNullFunction;
import javassist.CtClass;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static de.is24.javassist.CtClasses.getSuperclassOf;
import static de.is24.javassist.CtClasses.isJavaLangObject;

/**
 * Serves as a base class with which to mark classes as being in use if they are a direct subclass of one of the
 * specified classes.
 *
 * @since 1.4
 */
public abstract class SuperClassAnalyzer extends ByteCodeAnalyzer {

    private final String dependerId;
    private final NonNullFunction<AnalysisContext, Set<String>> supplySuperClassesFoundInClassPath;

    private SuperClassAnalyzer(@Nonnull String dependerId, @Nonnull Set<String> classNames) {
        checkArgument(!classNames.isEmpty(), "classNames cannot by empty!");
        this.dependerId = dependerId;
        supplySuperClassesFoundInClassPath = new ClassPathFilter(classNames);
    }

    /**
     * Creates a new <code>SuperClassAnalyzer</code>.
     *
     * @param dependerId a description of the <i>depending entity</i> with which to
     *                   call {@link de.is24.deadcode4j.AnalysisContext#addDependencies(String, Iterable)}
     * @param classNames a list of fully qualified class names indicating that the extending class is still in use
     * @since 1.4
     */
    protected SuperClassAnalyzer(@Nonnull String dependerId, @Nonnull String... classNames) {
        this(dependerId, Sets.newHashSet(classNames));
    }

    /**
     * Creates a new <code>SuperClassAnalyzer</code>.
     *
     * @param dependerId a description of the <i>depending entity</i> with which to
     *                   call {@link de.is24.deadcode4j.AnalysisContext#addDependencies(String, Iterable)}
     * @param classNames a list of fully qualified class names indicating that the extending class is still in use
     * @since 1.4
     */
    public SuperClassAnalyzer(String dependerId, Iterable<String> classNames) {
        this(dependerId, Sets.newHashSet(classNames));
    }

    @Override
    protected final void analyzeClass(@Nonnull AnalysisContext analysisContext, @Nonnull CtClass clazz) {
        Set<String> knownSuperClasses = getSuperClassesFoundInClassPath(analysisContext);
        if (knownSuperClasses.isEmpty()) {
            return;
        }

        String clazzName = clazz.getName();
        analysisContext.addAnalyzedClass(clazzName);

        if (!Collections.disjoint(knownSuperClasses, getClassHierarchy(clazz))) {
            analysisContext.addDependencies(this.dependerId, clazzName);
        }
    }

    @Nonnull
    private List<String> getClassHierarchy(@Nonnull final CtClass clazz) {
        List<String> classes = newArrayList();
        CtClass loopClass = clazz;
        do {
            classes.add(loopClass.getClassFile2().getSuperclass());
            loopClass = getSuperclassOf(loopClass);
        } while (loopClass != null && !isJavaLangObject(loopClass));
        return classes;
    }

    @Nonnull
    protected final Set<String> getSuperClassesFoundInClassPath(@Nonnull AnalysisContext analysisContext) {
        return analysisContext.getOrCreateCacheEntry(getClass(), supplySuperClassesFoundInClassPath);
    }

}
