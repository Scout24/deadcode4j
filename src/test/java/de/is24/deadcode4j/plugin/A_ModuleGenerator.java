package de.is24.deadcode4j.plugin;

import com.google.common.collect.Iterables;
import de.is24.deadcode4j.Module;
import de.is24.deadcode4j.Repository;
import de.is24.deadcode4j.junit.LoggingRule;
import de.is24.deadcode4j.junit.TempFileRule;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE_PLUS_RUNTIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public final class A_ModuleGenerator {
    @Rule
    public final LoggingRule enableLogging = new LoggingRule();
    @Rule
    public final TempFileRule tempFileRule = new TempFileRule();
    private MavenProject mavenProject;
    private RepositorySystem repositorySystem;
    private ModuleGenerator objectUnderTest;

    @Before
    public void setUp() throws Exception {
        mavenProject = givenMavenProject("project");
        repositorySystem = mock(RepositorySystem.class);
        disableArtifactResolving();

        objectUnderTest = new ModuleGenerator(repositorySystem);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void createsModuleForMavenProjectWithOutputDirectory() throws MojoExecutionException {
        Iterable<Module> modules = objectUnderTest.getModulesFor(singleton(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(1)));
        Module module = Iterables.getOnlyElement(modules);
        assertThat(module.getAllRepositories(), is(Matchers.<Repository>iterableWithSize(1)));
        Repository outputRepository = module.getOutputRepository();
        assertThat(outputRepository, is(notNullValue()));
        assertThat(mavenProject.getBuild().getOutputDirectory(),
                is(outputRepository.getDirectory().getAbsolutePath()));
    }

    @Test
    public void createsModuleForMavenProjectWithoutOutputDirectory() throws MojoExecutionException {
        mavenProject.getBuild().setOutputDirectory("/junit/foo/bar");

        Iterable<Module> modules = objectUnderTest.getModulesFor(singleton(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(1)));
        Module module = Iterables.getOnlyElement(modules);
        assertThat(module.getAllRepositories(), is(emptyIterable()));
    }

    @Test
    public void createsClassPathEntryForKnownProject() throws MojoExecutionException {
        MavenProject firstProject = givenMavenProject("firstProject");
        Artifact artifact = new ArtifactStub();
        artifact.setGroupId("de.is24.junit");
        artifact.setArtifactId("firstProject");
        artifact.setVersion("42");
        artifact.setScope("compile");
        mavenProject.setArtifacts(newHashSet(artifact));

        Iterable<Module> modules = objectUnderTest.getModulesFor(asList(firstProject, mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(2)));
        Module module = Iterables.getLast(modules);
        assertThat(module.getClassPath(), is(Matchers.<File>iterableWithSize(1)));
    }

    @Test
    public void createsClassPathEntryForResolvedDependency() throws MojoExecutionException {
        addResolvedArtifact(mavenProject);

        Iterable<Module> modules = objectUnderTest.getModulesFor(singleton(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(1)));
        Module module = Iterables.getOnlyElement(modules);
        assertThat(module.getClassPath(), is(Matchers.<File>iterableWithSize(1)));
    }

    @Test
    public void createsClassPathEntryForUnresolvedDependency() throws MojoExecutionException {
        addUnresolvedArtifact(mavenProject);
        enableArtifactResolving();

        Iterable<Module> modules = objectUnderTest.getModulesFor(singleton(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(1)));
        Module module = Iterables.getOnlyElement(modules);
        assertThat(module.getClassPath(), is(Matchers.<File>iterableWithSize(1)));
    }

    @Test
    public void createsNoClassPathEntryForUnresolvableDependency() throws MojoExecutionException {
        addUnresolvedArtifact(mavenProject);

        Iterable<Module> modules = objectUnderTest.getModulesFor(singleton(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(1)));
        Module module = Iterables.getOnlyElement(modules);
        assertThat(module.getClassPath(), is(emptyIterable()));
    }

    @Test
    public void createsNoClassPathEntryForInvalidDependency() throws MojoExecutionException {
        addResolvedArtifact(mavenProject).setFile(null);

        Iterable<Module> modules = objectUnderTest.getModulesFor(singleton(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(1)));
        Module module = Iterables.getOnlyElement(modules);
        assertThat(module.getClassPath(), is(emptyIterable()));
    }

    private MavenProject givenMavenProject(String projectId) {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setGroupId("de.is24.junit");
        mavenProject.setArtifactId(projectId);
        mavenProject.setVersion("42");
        mavenProject.getProperties().setProperty("project.build.sourceEncoding", "UTF-8");
        ArtifactStub projectArtifact = new ArtifactStub();
        projectArtifact.setGroupId("de.is24.junit");
        projectArtifact.setArtifactId(projectId);
        projectArtifact.setVersion("42");
        mavenProject.setArtifact(projectArtifact);
        Build build = new Build();
        build.setOutputDirectory(tempFileRule.getTempFile().getParent());
        mavenProject.setBuild(build);
        return mavenProject;
    }

    private Artifact addResolvedArtifact(MavenProject mavenProject) {
        return addArtifact(mavenProject, true);
    }

    private void addUnresolvedArtifact(MavenProject mavenProject) {
        addArtifact(mavenProject, false);
    }

    private Artifact addArtifact(MavenProject mavenProject, final boolean resolved) {
        Artifact artifact = new ArtifactStub() {
            private boolean resolved = false;

            @Override
            public boolean isResolved() {
                return this.resolved;
            }

            @Override
            public void setResolved(boolean b) {
                this.resolved = b;
            }

            @Override
            public File getFile() {
                return isResolved() ? super.getFile() : null;
            }
        };
        artifact.setGroupId("de.is24.junit");
        artifact.setArtifactId("dependency");
        artifact.setVersion("42");
        artifact.setScope("compile");
        artifact.setResolved(resolved);
        artifact.setFile(tempFileRule.getTempFile());

        mavenProject.setArtifactFilter(new ScopeArtifactFilter(SCOPE_COMPILE_PLUS_RUNTIME));
        if (resolved) {
            mavenProject.setResolvedArtifacts(newHashSet(artifact));
        } else {
            mavenProject.setArtifacts(newHashSet(artifact));
        }
        return artifact;
    }

    private void disableArtifactResolving() {
        when(this.repositorySystem.resolve(any(ArtifactResolutionRequest.class))).thenAnswer(new Answer<ArtifactResolutionResult>() {
            @Override
            public ArtifactResolutionResult answer(InvocationOnMock invocationOnMock) throws Throwable {
                ArtifactResolutionRequest request = (ArtifactResolutionRequest) invocationOnMock.getArguments()[0];
                ArtifactResolutionResult artifactResolutionResult = new ArtifactResolutionResult();
                artifactResolutionResult.addMissingArtifact(request.getArtifact());
                return artifactResolutionResult;
            }
        });
    }

    private void enableArtifactResolving() {
        reset(this.repositorySystem);
        when(this.repositorySystem.resolve(any(ArtifactResolutionRequest.class))).thenAnswer(new Answer<ArtifactResolutionResult>() {
            @Override
            public ArtifactResolutionResult answer(InvocationOnMock invocationOnMock) throws Throwable {
                ArtifactResolutionRequest request = (ArtifactResolutionRequest) invocationOnMock.getArguments()[0];
                request.getArtifact().setResolved(true);
                return new ArtifactResolutionResult();
            }
        });
    }

}