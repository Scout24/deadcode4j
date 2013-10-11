package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * The <code>CodeContext</code> provides the capability to
 * {@link #addAnalyzedClass(String) report the existence of code} and
 * {@link #addDependencies(String, java.util.Collection) the dependencies of it}.
 *
 * @since 1.1.0
 */
public class CodeContext {

    private final Set<String> analyzedClasses = newHashSet();
    private final Map<String, Set<String>> dependencyMap = newHashMap();

    /**
     * Report code dependencies.
     *
     * @param depender  the depending entity, e.g. a class or a more conceptual entity like Spring XML files or a web.xml;
     *                  the latter should somehow be marked as such, e.g. "_Spring_"
     * @param dependees the classes being depended upon
     * @since 1.1.0
     */
    public void addDependencies(@Nonnull String depender, @Nonnull Collection<String> dependees) {
        Set<String> existingDependees = dependencyMap.get(depender);
        if (existingDependees == null) {
            existingDependees = new HashSet<String>();
            dependencyMap.put(depender, existingDependees);
        }
        existingDependees.addAll(dependees);
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
     * {@link #addDependencies(String, java.util.Collection)}.
     *
     * @since 1.1.0
     */
    @Nonnull
    public AnalyzedCode getAnalyzedCode() {
        return new AnalyzedCode(this.analyzedClasses, this.dependencyMap);
    }

}
