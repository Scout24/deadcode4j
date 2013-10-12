package de.is24.deadcode4j;

import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

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

}
