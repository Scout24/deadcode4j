package de.is24.maven;

import com.google.common.base.Optional;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static com.google.common.base.Optional.absent;

/**
 * Performs an update check for the currently executed plugin.
 *
 * @since 2.0.0
 */
@Component(role = UpdateChecker.class)
public class UpdateChecker {

    @Requirement
    private LegacySupport legacySupport;
    @Requirement
    private RepositoryMetadataManager repositoryMetadataManager;

    /**
     * Checks if a new version is available for the currently executed plugin.
     *
     * @return the most recent version available or nothing if the running plugin is of newer or equal version
     * @since 2.0.0
     */
    @Nonnull
    public Optional<ArtifactVersion> checkForUpdate(@Nonnull MojoExecution mojoExecution) {
        Logger logger = LoggerFactory.getLogger(getClass());
        try {
            if (legacySupport.getSession().isOffline()) {
                logger.info("Running in offline mode; skipping update check.");
                return absent();
            }
            Artifact artifact = ArtifactUtils.copyArtifact(
                    mojoExecution.getMojoDescriptor().getPluginDescriptor().getPluginArtifact());
            ArtifactVersion versionInUse = artifact.getVersionRange().getRecommendedVersion();
            ArtifactRepositoryMetadata repositoryMetadata = new ArtifactRepositoryMetadata(artifact);
            repositoryMetadataManager.resolve(repositoryMetadata, legacySupport.getSession().getCurrentProject().getPluginArtifactRepositories(), legacySupport.getSession().getLocalRepository());
            ArtifactVersion newestVersion = versionInUse;
            for (String version : repositoryMetadata.getMetadata().getVersioning().getVersions()) {
                ArtifactVersion artifactVersion = new DefaultArtifactVersion(version);
                if ("SNAPSHOT".equals(artifactVersion.getQualifier())) {
                    continue;
                }
                if (artifactVersion.compareTo(newestVersion) > 0) {
                    newestVersion = artifactVersion;
                }
            }
            if (versionInUse.compareTo(newestVersion) >= 0) {
                logger.debug("Running latest version.");
            } else {
                logger.debug("New plugin version [{}] is available.", newestVersion);
                return Optional.of(newestVersion);
            }
        } catch (Exception e) {
            logger.debug("Update check failed!", e);
            logger.warn("Update check failed: {}", e.getMessage());
        }
        return absent();
    }

}
