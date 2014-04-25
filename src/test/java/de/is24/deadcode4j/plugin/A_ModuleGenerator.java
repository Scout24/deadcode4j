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

import java.io.File;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE_PLUS_RUNTIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
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
        objectUnderTest = new ModuleGenerator(repositorySystem);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void createsModuleForMavenProjectWithOutputDirectory() throws MojoExecutionException {
        Iterable<Module> modules = objectUnderTest.getModulesFor(asList(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(1)));
        Module module = Iterables.getOnlyElement(modules);
        assertThat(module.getAllRepositories(), is(Matchers.<Repository>iterableWithSize(1)));
        Repository outputRepository = module.getOutputRepository();
        assertThat(outputRepository, is(notNullValue()));
        assertThat(mavenProject.getBuild().getOutputDirectory(),
                is(outputRepository.getDirectory().getAbsolutePath()));
    }

    @Test
    public void createsNoModuleForMavenProjectWithoutOutputDirectory() throws MojoExecutionException {
        mavenProject.getBuild().setOutputDirectory("/junit/foo/bar");

        Iterable<Module> modules = objectUnderTest.getModulesFor(asList(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(0)));
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

        Iterable<Module> modules = objectUnderTest.getModulesFor(asList(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(1)));
        Module module = Iterables.getOnlyElement(modules);
        assertThat(module.getClassPath(), is(Matchers.<File>iterableWithSize(1)));
    }

    @Test
    public void createsClassPathEntryForUnresolvedDependency() throws MojoExecutionException {
        addUnresolvedArtifact(mavenProject);
        ArtifactResolutionResult artifactResolutionResult = new ArtifactResolutionResult();
        when(this.repositorySystem.resolve(any(ArtifactResolutionRequest.class))).thenReturn(artifactResolutionResult);

        Iterable<Module> modules = objectUnderTest.getModulesFor(asList(mavenProject));

        assertThat(modules, is(Matchers.<Module>iterableWithSize(1)));
        Module module = Iterables.getOnlyElement(modules);
        assertThat(module.getClassPath(), is(Matchers.<File>iterableWithSize(1)));
    }

    private MavenProject givenMavenProject(String projectId) {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setGroupId("de.is24.junit");
        mavenProject.setArtifactId(projectId);
        mavenProject.setVersion("42");
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

    private void addResolvedArtifact(MavenProject mavenProject) {
        addArtifact(mavenProject, true);
    }

    private void addUnresolvedArtifact(MavenProject mavenProject) {
        addArtifact(mavenProject, false);
    }

    private void addArtifact(MavenProject mavenProject, final boolean resolved) {
        Artifact artifact = new ArtifactStub() {
            @Override
            public boolean isResolved() {
                return resolved;
            }
        };
        artifact.setGroupId("de.is24.junit");
        artifact.setArtifactId("dependency");
        artifact.setVersion("42");
        artifact.setScope("compile");
        artifact.setFile(tempFileRule.getTempFile());

        mavenProject.setResolvedArtifacts(newHashSet(artifact));
        mavenProject.setArtifactFilter(new ScopeArtifactFilter(SCOPE_COMPILE_PLUS_RUNTIME));
    }

}