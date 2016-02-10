package de.is24.deadcode4j;

import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.getOrAddMappedSet;
import static java.util.Arrays.asList;

/**
 * The <code>AnalysisSink</code> provides the capability to
 * {@link #addAnalyzedClass(String) report the existence of code} and
 * {@link #addDependencies(String, Iterable) the dependencies of it}.
 * It also allows to notify of {@link #addException(AnalysisStage) exceptions that occurred}.
 *
 * @since 2.0.0
 */
public class AnalysisSink {
    @Nonnull
    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    @Nonnull
    private final Set<String> analyzedClasses = newHashSet();
    @Nonnull
    private final Map<String, Set<String>> dependencyMap = newHashMap();
    @Nonnull
    private final EnumSet<AnalysisStage> stagesWithExceptions = EnumSet.noneOf(AnalysisStage.class);

    @Override
    public String toString() {
        return "AnalysisSink";
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
        if (size(dependees) == 0) {
            return;
        }
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
     * Indicate that an exception occurred at the given stage.
     *
     * @since 2.0.0
     */
    public void addException(@Nonnull AnalysisStage stage) {
        this.stagesWithExceptions.add(stage);
    }

    /**
     * Computes the {@link AnalyzedCode} based on the reports being made via {@link #addAnalyzedClass(String)} and
     * {@link #addDependencies(String, Iterable)}.
     *
     * @since 1.1.0
     */
    @Nonnull
    public AnalyzedCode getAnalyzedCode() {
        return new AnalyzedCode(this.stagesWithExceptions, this.analyzedClasses, this.dependencyMap);
    }

}
