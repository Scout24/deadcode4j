package de.is24.deadcode4j;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Provides convenience methods.
 *
 * @since 1.2.0
 */
public final class Utils {

    private Utils() {}

    /**
     * Returns <i>groupId:artifactId</i> for the specified artifact.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static String getKeyFor(@Nonnull Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId();
    }

    /**
     * Returns <i>groupId:artifactId:version</i> for the specified artifact.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static String getVersionedKeyFor(@Nonnull Artifact artifact) {
        return getKeyFor(artifact) + ":" + artifact.getVersion();
    }

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
     * Returns {@code true} if the given collection is {@code null} or empty.
     *
     * @since 2.0.0
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
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

    /**
     * Retrieves an existing <code>Set</code> being mapped by the specified key or puts a new one into the map.
     *
     * @since 1.4
     */
    public static <K, V> Set<V> getOrAddMappedSet(Map<K, Set<V>> map, K key) {
        Set<V> values = map.get(key);
        if (values == null) {
            values = newHashSet();
            map.put(key, values);
        }
        return values;
    }

    /**
     * Returns the given <code>Iterable</code> or an empty list if it is <code>null</code> as a {@link FluentIterable}.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static <E> FluentIterable<E> emptyIfNull(@Nullable Iterable<E> iterable) {
        return FluentIterable.from(iterable == null ? Collections.<E>emptyList() : iterable);
    }

    /**
     * A properly annotated implementation of {@link com.google.common.base.Preconditions#checkNotNull(Object)}.
     *
     * @since 2.0.0
     */
    @Nonnull
    @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
    @SuppressWarnings("PMD.AvoidThrowingNullPointerException")
    public static <T> T checkNotNull(@Nullable T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Indicates if a <code>String</code> is either <code>null</code>, empty or consists of whitespace only.
     *
     * @since 2.2.0
     */
    public static boolean isBlank(@Nullable String string) {
        // we simply delegate this - but we have it in one place
        return StringUtils.isBlank(string);
    }

}
