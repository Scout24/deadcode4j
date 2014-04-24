package de.is24.deadcode4j;

import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.getOrAddMappedSet;
import static java.util.Arrays.asList;

/**
 * The <code>CodeContext</code> provides the capability to
 * {@link #addAnalyzedClass(String) report the existence of code} and
 * {@link #addDependencies(String, Iterable)}  the dependencies of it}.
 *
 * @since 1.1.0
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class CodeContext {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    private final Set<String> analyzedClasses = newHashSet();
    private final Map<String, Set<String>> dependencyMap = newHashMap();
    private final Map<Object, Object> cache = newHashMap();
    private final Module module;

    /**
     * Creates a new instance of <code>CodeContext</code> for the specified module.
     *
     * @since 1.6
     */
    public CodeContext(Module module) {
        this.module = module;
    }

    /**
     * Returns the associated <code>Module</code>.
     *
     * @since 1.6
     */
    public Module getModule() {
        return module;
    }

    /**
     * Returns a <code>Map</code> that can be used to cache things or pass along between analyzers.
     *
     * @return a simple {@link java.util.Map}
     */
    public Map<Object, Object> getCache() {
        return cache;
    }

    /**
     * Report code dependencies.
     *
     * @param depender  the depending entity, e.g. a class or a more conceptual entity like Spring XML files or a web.xml;
     *                  the latter should somehow be marked as such, e.g. "_Spring_"
     * @param dependees the classes being depended upon
     * @see #addDependencies(String, String...)
     * @since 1.1.0
     */
    public void addDependencies(@Nonnull String depender, @Nonnull Iterable<String> dependees) {
        dependees = filter(dependees, not(equalTo(depender))); // this would be cheating
        Set<String> existingDependees = getOrAddMappedSet(this.dependencyMap, depender);
        for (String aDependee : dependees) {
            existingDependees.add(aDependee);
        }
        logger.debug("Added dependencies from [{}] to {}.", depender, dependees);
    }

    /**
     * Report code dependencies.
     *
     * @param depender  the depending entity, e.g. a class or a more conceptual entity like Spring XML files or a web.xml;
     *                  the latter should somehow be marked as such, e.g. "_Spring_"
     * @param dependees the classes being depended upon
     * @see #addDependencies(String, Iterable)
     * @since 1.4
     */
    public void addDependencies(@Nonnull String depender, @Nonnull String... dependees) {
        addDependencies(depender, asList(dependees));
    }

    /**
     * Report the existence of a class.
     *
     * @since 1.1.0
     */
    public void addAnalyzedClass(@Nonnull String clazz) {
        this.analyzedClasses.add(clazz);
    }

    /**
     * Computes the {@link AnalyzedCode} based on the reports being made via {@link #addAnalyzedClass(String)} and
     * {@link #addDependencies(String, Iterable)}.
     *
     * @since 1.1.0
     */
    @Nonnull
    public AnalyzedCode getAnalyzedCode() {
        return new AnalyzedCode(this.analyzedClasses, this.dependencyMap);
    }
}
