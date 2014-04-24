package de.is24.deadcode4j.analyzer;

import com.google.common.collect.Sets;
import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Serves as a base class with which to mark classes as being in use if they are a direct subclass of one of the
 * specified classes.
 *
 * @since 1.4
 */
public abstract class SuperClassAnalyzer extends ByteCodeAnalyzer implements Analyzer {

    private final String dependerId;
    private final Collection<String> superClasses;

    private SuperClassAnalyzer(@Nonnull String dependerId, @Nonnull Collection<String> classNames) {
        this.dependerId = dependerId;
        this.superClasses = classNames;
        checkArgument(!this.superClasses.isEmpty(), "classNames cannot by empty!");
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
        String clazzName = clazz.getName();
        codeContext.addAnalyzedClass(clazzName);
        final List<String> classHierarchy;
        try {
            classHierarchy = getClassHierarchy(clazz);
        } catch (NotFoundException e) {
            logger.warn("The class path is not correctly set up! Skipping superclass check for {}.", clazzName, e);
            return;
        }
        if (!Collections.disjoint(this.superClasses, classHierarchy)) {
            codeContext.addDependencies(this.dependerId, clazzName);
        }
    }

}
