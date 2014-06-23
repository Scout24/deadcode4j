package de.is24.maven;

import com.google.common.base.Optional;
import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

public final class An_UpdateChecker {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();
    private UpdateChecker objectUnderTest;
    private RepositoryMetadataManager repositoryMetadataManager;
    private MavenSession mavenSession;
    private Optional<ArtifactVersion> result;

    private static ArtifactVersion version(String version) {
        return new DefaultArtifactVersion(version);
    }

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new UpdateChecker();
        repositoryMetadataManager = mock(RepositoryMetadataManager.class);
        mavenSession = mock(MavenSession.class);
        when(mavenSession.getCurrentProject()).thenReturn(new MavenProject());
        LegacySupport legacySupport = mock(LegacySupport.class);
        when(legacySupport.getSession()).thenReturn(mavenSession);
        ReflectionUtils.setVariableValueInObject(objectUnderTest, "legacySupport", legacySupport);
        ReflectionUtils.setVariableValueInObject(objectUnderTest, "repositoryMetadataManager",
                repositoryMetadataManager);
    }

    @Test
    public void returnsTheMostRecentVersion() {
        givenOnlineMode();
        givenAvailableVersions("1", "10.0", "42", "42.22.9", "42.23");

        whenCheckingForUpdate();

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(equalTo(version("42.23"))));
    }

    @Test
    public void returnsNothingIfLatestVersionIsRunning() {
        givenOnlineMode();
        givenAvailableVersions("1", "10.0", "22", "22.9", "23");

        whenCheckingForUpdate();

        thenNothingIsReturned();
    }

    @Test
    public void ignoresSnapshotVersions() {
        givenOnlineMode();
        givenAvailableVersions("23", "24-SNAPSHOT");

        whenCheckingForUpdate();

        thenNothingIsReturned();
    }

    @Test
    public void returnsNothingIfInOfflineMode() {
        givenOfflineMode();
        givenAvailableVersions("1", "10.0", "42", "42.22.9", "42.23");

        whenCheckingForUpdate();

        thenNothingIsReturned();
    }

    @Test
    public void returnsNothingIfExceptionOccurs() {
        givenOnlineMode();

        whenCheckingForUpdate();

        thenNothingIsReturned();
    }

    private void givenOnlineMode() {
        when(mavenSession.isOffline()).thenReturn(false);
    }

    private void givenOfflineMode() {
        when(mavenSession.isOffline()).thenReturn(true);
    }

    private void givenAvailableVersions(final String... availableVersions) {
        try {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    RepositoryMetadata repositoryMetadata = (RepositoryMetadata) invocation.getArguments()[0];
                    Versioning versioning = new Versioning();
                    versioning.setVersions(Arrays.asList(availableVersions));
                    repositoryMetadata.getMetadata().setVersioning(versioning);
                    return null;
                }
            }).when(repositoryMetadataManager).resolve(any(RepositoryMetadata.class),
                    anyListOf(ArtifactRepository.class), any(ArtifactRepository.class));
        } catch (RepositoryMetadataResolutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void whenCheckingForUpdate() {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();
        pluginDescriptor.setPluginArtifact(new DefaultArtifact("de.is24", "junit", "23", null, "maven-plugin", "", null));
        MojoDescriptor mojoDescriptor = new MojoDescriptor();
        mojoDescriptor.setPluginDescriptor(pluginDescriptor);
        result = objectUnderTest.checkForUpdate(new MojoExecution(mojoDescriptor));
    }

    private void thenNothingIsReturned() {
        assertThat(result.isPresent(), is(false));
    }

}