package de.is24.deadcode4j.analyzer;

import com.google.common.collect.Sets;
import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPathFilter;
import de.is24.guava.NonNullFunction;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Serves as a base class with which to mark classes as being in use if they are a direct subclass of one of the
 * specified classes.
 *
 * @since 1.4
 */
public abstract class SuperClassAnalyzer extends ByteCodeAnalyzer {

    private final String dependerId;
    private final NonNullFunction<CodeContext, Set<String>> supplySuperClassesFoundInClassPath;

    private SuperClassAnalyzer(@Nonnull String dependerId, @Nonnull Set<String> classNames) {
        checkArgument(!classNames.isEmpty(), "classNames cannot by empty!");
        this.dependerId = dependerId;
        supplySuperClassesFoundInClassPath = new ClassPathFilter(classNames);
    }

    /**
     * Creates a new <code>SuperClassAnalyzer</code>.
     *
     * @param dependerId a description of the <i>depending entity</i> with which to
     *                   call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, Iterable)}
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
     *                   call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, Iterable)}
     * @param classNames a list of fully qualified class names indicating that the extending class is still in use
     * @since 1.4
     */
    public SuperClassAnalyzer(String dependerId, Iterable<String> classNames) {
        this(dependerId, Sets.newHashSet(classNames));
    }

    @Override
    protected final void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        Set<String> knownSuperClasses = getSuperClassesFoundInClassPath(codeContext);
        if (knownSuperClasses.isEmpty()) {
            return;
        }
        String clazzName = clazz.getName();
        codeContext.addAnalyzedClass(clazzName);
        final List<String> classHierarchy;
        try {
            classHierarchy = getClassHierarchy(clazz);
        } catch (NotFoundException e) {
            logger.warn("The class path is not correctly set up; could not load [{}]! Skipping superclass check for {}.", e.getMessage(), clazzName);
            return;
        }
        if (!Collections.disjoint(knownSuperClasses, classHierarchy)) {
            codeContext.addDependencies(this.dependerId, clazzName);
        }
    }

    @Nonnull
    private List<String> getClassHierarchy(@Nonnull final CtClass clazz) throws NotFoundException {
        List<String> classes = newArrayList();
        CtClass loopClass = clazz;
        do {
            classes.add(loopClass.getClassFile2().getSuperclass());
            loopClass = loopClass.getSuperclass();
        } while (loopClass != null && !"java.lang.Object".equals(loopClass.getName()));
        return classes;
    }

    @Nonnull
    protected final Set<String> getSuperClassesFoundInClassPath(@Nonnull CodeContext codeContext) {
        return codeContext.getOrCreateCacheEntry(getClass(), supplySuperClassesFoundInClassPath);
    }

}
