package de.is24.maven;

import com.google.common.base.Optional;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.plugin.MojoExecution;

import javax.annotation.Nonnull;

/**
 * Performs an update check for the currently executed plugin.
 *
 * @since 1.6
 */
public interface UpdateChecker {

    /**
     * Checks if a new version is available for the currently executed plugin.
     *
     * @return the most recent version available or nothing if the running plugin is a newer or equal version
     * @since 1.6
     */
    @Nonnull
    Optional<ArtifactVersion> checkForUpdate(@Nonnull MojoExecution mojoExecution);

}
