package de.is24.deadcode4j;

import de.is24.guava.NonNullFunction;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
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
    @Nonnull
    private final Set<String> analyzedClasses = newHashSet();
    @Nonnull
    private final Map<String, Set<String>> dependencyMap = newHashMap();
    @Nonnull
    private final Map<Object, Object> cache = newHashMap();
    @Nonnull
    private final Module module;
    @Nonnull
    private final Map<Object, IntermediateResult> intermediateResults;
    @Nonnull
    private final EnumSet<AnalysisStage> stagesWithExceptions = EnumSet.noneOf(AnalysisStage.class);

    /**
     * Creates a new instance of <code>CodeContext</code> for the specified module.
     *
     * @since 1.6
     */
    public CodeContext(@Nonnull Module module, @Nonnull Map<Object, IntermediateResult> intermediateResults) {
        this.module = module;
        this.intermediateResults = newHashMap(intermediateResults);
    }

    @Override
    public String toString() {
        return "CodeContext for [" + this.module + "]";
    }

    /**
     * Returns the associated <code>Module</code>.
     *
     * @since 1.6
     */
    @Nonnull
    public Module getModule() {
        return module;
    }

    /**
     * Returns a <code>Map</code> that can be used to cache things or pass along between analyzers.
     *
     * @return a simple {@link java.util.Map}
     */
    @Nonnull
    public Map<Object, Object> getCache() {
        return cache;
    }

    @Nonnull
    public <T> T getOrCreateCacheEntry(Object key, NonNullFunction<CodeContext, T> supplier) {
        @SuppressWarnings("unchecked")
        T entry = (T) this.cache.get(key);
        if (entry == null) {
            entry = supplier.apply(this);
            this.cache.put(key, entry);
        }
        return entry;
    }

    @Nullable
    public IntermediateResult getIntermediateResult(@Nonnull Object key) {
        return this.intermediateResults.get(key);
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
        return new AnalyzedCode(this.stagesWithExceptions, this.analyzedClasses, this.dependencyMap);
    }

    /**
     * Indicate that an exception occurred at the given stage.
     *
     * @since 1.6
     */
    public void addException(@Nonnull AnalysisStage stage) {
        this.stagesWithExceptions.add(stage);
    }

}
