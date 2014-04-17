package de.is24.deadcode4j.plugin.packaginghandler;

import de.is24.deadcode4j.CodeRepository;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.Callable;

import static com.google.common.collect.Lists.newArrayList;
import static de.is24.deadcode4j.Utils.getKeyFor;

/**
 * The <code>DefaultPackagingHandler</code> returns the default output directory if it exists.
 *
 * @since 1.2.0
 */
public class DefaultPackagingHandler extends PackagingHandler {

    public DefaultPackagingHandler(Callable<Log> logAccessor) {
        super(logAccessor);
    }

    @Override
    @Nonnull
    public Collection<CodeRepository> getCodeRepositoriesFor(@Nonnull MavenProject project) {
        Collection<CodeRepository> repositories = newArrayList();
        addOutputDirectory(repositories, project);
        addJavaFilesOfSourceDirectories(repositories, project);
        return repositories;
    }

    private void addOutputDirectory(@Nonnull Collection<CodeRepository> repositories, @Nonnull MavenProject project) {
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        if (outputDirectory.exists()) {
            repositories.add(new CodeRepository(outputDirectory));
            if (getLog().isDebugEnabled()) {
                getLog().debug("Going to analyze output directory [" + outputDirectory + "].");
            }
        } else {
            getLog().warn("The output directory of " + getKeyFor(project) +
                    " does not exist - assuming the project simply has nothing to provide!");
        }
    }

}
