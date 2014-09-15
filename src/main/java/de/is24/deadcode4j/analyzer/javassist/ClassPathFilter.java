package de.is24.deadcode4j.analyzer.javassist;

import de.is24.deadcode4j.AnalysisContext;
import de.is24.guava.NonNullFunction;
import javassist.ClassPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;

/**
 * This function returns a set containing only those classes that exist within the class path of the specified context.
 * It is intended to be used in conjunction with
 * {@link de.is24.deadcode4j.AnalysisContext#getOrCreateCacheEntry(Object, de.is24.guava.NonNullFunction)}.
 *
 * @since 2.0.0
 */
public class ClassPathFilter implements NonNullFunction<AnalysisContext, Set<String>> {
    @Nonnull
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Nonnull
    private final Set<String> classes;

    /**
     * Creates a new <code>ClassPathFilter</code> for the given class names.
     *
     * @since 2.0.0
     */
    public ClassPathFilter(@Nonnull Set<String> classes) {
        this.classes = classes;
    }

    @Nonnull
    @Override
    public Set<String> apply(@Nonnull AnalysisContext input) {
        ClassPool classPool = classPoolAccessorFor(input).getClassPool();
        Set<String> knownClasses = newHashSet();
        for (String className : this.classes) {
            if (classPool.find(className) != null) {
                knownClasses.add(className);
            }
        }
        logger.debug("Found those classes in the class path: {}", knownClasses);
        return knownClasses;
    }

}
