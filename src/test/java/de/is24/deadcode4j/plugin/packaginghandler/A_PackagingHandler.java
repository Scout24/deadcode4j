package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.CodeRepository;
import de.is24.deadcode4j.junit.FileLoader;
import de.is24.deadcode4j.junit.TestLog;
import org.apache.commons.io.DirectoryWalker;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class A_PackagingHandler {

    private PackagingHandler objectUnderTest;

    private static Callable<Log> logAccessor() {
        return new Callable<Log>() {
            private final Log log = new TestLog();

            @Override
            public Log call() throws Exception {
                return log;
            }
        };
    }

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new PackagingHandler(logAccessor()) {
            @Nonnull
            @Override
            public Collection<CodeRepository> getCodeRepositoriesFor(@Nonnull MavenProject project) throws MojoExecutionException {
                ArrayList<CodeRepository> repositories = newArrayList();
                addJavaFilesOfSourceDirectories(repositories, project);
                return repositories;
            }
        };
    }

    @Test
    public void addsNoCodeRepositoryIfMavenProjectDoesNotExist() throws MojoExecutionException {
        @SuppressWarnings("ConstantConditions")
        Collection<CodeRepository> codeRepositoriesFor = objectUnderTest.getCodeRepositoriesFor(null);

        assertThat(codeRepositoriesFor, is(empty()));
    }

    @Test
    public void addsNoCodeRepositoryIfThereAreNoCompileSourceRoots() throws MojoExecutionException {
        MavenProject mavenProject = new MavenProject() {
            {
                setCompileSourceRoots(null);
            }
        };
        Collection<CodeRepository> codeRepositoriesFor = objectUnderTest.getCodeRepositoriesFor(mavenProject);

        assertThat(codeRepositoriesFor, is(empty()));
    }

    @Test
    public void addsCompileSourceRootsAsCodeRepositories() throws MojoExecutionException, IOException {
        MavenProject mavenProject = new MavenProject();
        mavenProject.getCompileSourceRoots().add(directoryThisClassIsLocated());
        Collection<CodeRepository> codeRepositories = objectUnderTest.getCodeRepositoriesFor(mavenProject);

        assertThat(codeRepositories, hasSize(1));

        CodeRepository repository = codeRepositories.iterator().next();
        Collection<String> results = getFileNames(repository);
        assertThat(results, hasItem(getClass().getSimpleName() + ".java"));
    }

    @Test
    public void addsCompileSourceRootsOnlyIfTheyExist() throws MojoExecutionException, IOException {
        MavenProject mavenProject = new MavenProject();
        mavenProject.getCompileSourceRoots().add("thisDirectory/is/so/never/gone/exist");
        Collection<CodeRepository> codeRepositories = objectUnderTest.getCodeRepositoriesFor(mavenProject);

        assertThat(codeRepositories, is(empty()));
    }

    private String directoryThisClassIsLocated() {
        return FileLoader.getFile("../../src/test/java/de/is24/deadcode4j/plugin/packaginghandler").getAbsolutePath();
    }

    private Collection<String> getFileNames(final CodeRepository repository) throws IOException {
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
