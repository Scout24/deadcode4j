package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.Repository;
import de.is24.deadcode4j.junit.FileLoader;
import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.commons.io.DirectoryWalker;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.project.MavenProject;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_PackagingHandler {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();
    private PackagingHandler objectUnderTest;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new PackagingHandler() {
            @Nullable
            @Override
            public Repository getOutputRepositoryFor(@Nonnull MavenProject project) throws MojoExecutionException {
                return null;
            }

            @Nonnull
            @Override
            public Iterable<Repository> getAdditionalRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException {
                return getJavaFilesOfCompileSourceRootsAsRepositories(project);
            }
        };
    }

    @Test
    public void addsNoRepositoryIfThereAreNoCompileSourceRoots() throws MojoExecutionException {
        MavenProjectStub mavenProject = new MavenProjectStub();
        mavenProject.setCompileSourceRoots(null);

        Iterable<Repository> repositories = objectUnderTest.getAdditionalRepositoriesFor(mavenProject);

        assertThat(repositories, is(emptyIterable()));
    }

    @Test
    public void addsCompileSourceRootsAsCodeRepositories() throws MojoExecutionException, IOException {
        MavenProject mavenProject = new MavenProject();
        mavenProject.getCompileSourceRoots().add(directoryThisClassIsLocated());

        Iterable<Repository> repositories = objectUnderTest.getAdditionalRepositoriesFor(mavenProject);

        assertThat(repositories, is(Matchers.<Repository>iterableWithSize(1)));
        Repository repository = repositories.iterator().next();
        Collection<String> results = getFileNames(repository);
        assertThat(results, hasItem(getClass().getSimpleName() + ".java"));
    }

    @Test
    public void addsCompileSourceRootsOnlyIfTheyExist() throws MojoExecutionException, IOException {
        MavenProject mavenProject = new MavenProject();
        mavenProject.getCompileSourceRoots().add("thisDirectory/is/so/never/going/to/exist");

        Iterable<Repository> repositories = objectUnderTest.getAdditionalRepositoriesFor(mavenProject);

        assertThat(repositories, is(emptyIterable()));
    }

    private String directoryThisClassIsLocated() {
        return FileLoader.getFile("../../src/test/java/de/is24/deadcode4j/plugin/packaginghandler").getAbsolutePath();
    }

    private Collection<String> getFileNames(final Repository repository) throws IOException {
        return new DirectoryWalker<String>(repository.getFileFilter(), -1) {
            @Override
            protected void handleFile(File file, int depth, Collection<String> results) throws IOException {
                if (file.isFile()) {
                    results.add(file.getName());
                }
            }

            public Collection<String> go(File directory) throws IOException {
                Collection<String> results = newArrayList();
                super.walk(directory, results);
                return results;
            }
        }.go(repository.getDirectory());
    }

}