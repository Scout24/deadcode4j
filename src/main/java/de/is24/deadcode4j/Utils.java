package de.is24.deadcode4j;

import com.google.common.base.Function;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Provides convenience methods.
 *
 * @since 1.2.0
 */
public final class Utils {

    /**
     * Returns <i>groupId:artifactId</i> for the specified project.
     *
     * @since 1.2.0
     */
    @Nonnull
    public static String getKeyFor(@Nonnull MavenProject project) {
        return project.getGroupId() + ":" + project.getArtifactId();
    }

    /**
     * Returns a <code>Function</code> transforming a <code>MavenProject</code> into it's
     * {@link #getKeyFor(org.apache.maven.project.MavenProject) key representation}.
     *
     * @see #getKeyFor(org.apache.maven.project.MavenProject)
     * @since 1.4
     */
    @Nonnull
    public static Function<MavenProject, String> toKey() {
        return new Function<MavenProject, String>() {
            @Override
            public String apply(@Nullable MavenProject input) {
                return input == null ? null : getKeyFor(input);
            }
        };
    }

    /**
     * Adds the given element to a collection if the element is not <code>null</code>.
     *
     * @since 1.2.0
     */
    public static <E> boolean addIfNonNull(@Nonnull Collection<E> collection, @Nullable E element) {
        return element != null && collection.add(element);
    }

    /**
     * Returns a map's value for the specified key or the given default value if the value is <code>null</code>.
     *
     * @since 1.2.0
     */
    public static <K, V> V getValueOrDefault(Map<K, V> map, K key, V defaultValue) {
        V value = map.get(key);
        return value != null ? value : defaultValue;
    }

    private Utils() {
        super();
    }

    /**
     * Adds the given value to a <code>Set</code> being mapped by the specified key - and creates a new <code>Set</code>
     * if the map does not already contain one.
     *
     * @since 1.4
     */
    public static <K, V> void addToMappedSet(Map<K, Set<V>> map, K key, V value) {
        Set<V> values = map.get(key);
        if (values == null) {
            values = newHashSet();
            map.put(key, values);
        }
        values.add(value);
    }

}
