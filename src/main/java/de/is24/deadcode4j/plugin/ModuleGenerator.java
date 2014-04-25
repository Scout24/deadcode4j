package de.is24.deadcode4j.plugin;

import de.is24.deadcode4j.Module;
import de.is24.deadcode4j.Repository;
import de.is24.deadcode4j.plugin.packaginghandler.DefaultPackagingHandler;
import de.is24.deadcode4j.plugin.packaginghandler.PackagingHandler;
import de.is24.deadcode4j.plugin.packaginghandler.PomPackagingHandler;
import de.is24.deadcode4j.plugin.packaginghandler.WarPackagingHandler;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static de.is24.deadcode4j.Utils.getKeyFor;
import static de.is24.deadcode4j.Utils.getValueOrDefault;

/**
 * Calculates the modules for the given maven projects.
 *
 * @see #getModulesFor(Iterable)
 * @since 1.6
 */
class ModuleGenerator {

    @Nonnull
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Nonnull
    private final PackagingHandler defaultPackagingHandler = new DefaultPackagingHandler();
    @Nonnull
    private final Map<String, PackagingHandler> packagingHandlers = newHashMap();
    @Nonnull
    private final RepositorySystem repositorySystem;

    /**
     * Creates a new <code>ModuleGenerator</code>.
     *
     * @param repositorySystem the given <code>RepositorySystem</code> is required to resolve the class path of the
     *                         examined maven projects
     * @since 1.6
     */
    public ModuleGenerator(@Nonnull RepositorySystem repositorySystem) {
        this.repositorySystem = repositorySystem;
        packagingHandlers.put("pom", new PomPackagingHandler());
        packagingHandlers.put("war", new WarPackagingHandler());
    }

    /**
     * Attempts to create a <code>Module</code> for each given maven project. A project providing no repository will be
     * silently ignored.
     *
     * @param projects the <code>MavenProject</code>s to transform; the projects MUST be ordered such that project
     *                 <i>A</i> is listed before project <i>B</i> if <i>B</i> depends on <i>A</i>
     * @throws MojoExecutionException if calculating the repositories fails
     * @since 1.6
     */
    @Nonnull
    public Iterable<Module> getModulesFor(@Nonnull Iterable<MavenProject> projects) throws MojoExecutionException {
        List<Module> modules = newArrayList();
        Map<String, Module> knownModules = newHashMap();
        for (MavenProject project : projects) {
            Module module = getModuleFor(project, knownModules);
            knownModules.put(getKeyFor(project.getArtifact()), module);
            if (size(module.getAllRepositories()) > 0) {
                modules.add(module);
                logger.debug("Added {} for {}.", module, project);
            } else {
                logger.debug("No repositories available for {}.", project);
            }
        }
        return modules;
    }

    @Nonnull
    private Module getModuleFor(@Nonnull MavenProject project, Map<String, Module> knownModules) throws MojoExecutionException {
        PackagingHandler packagingHandler =
                getValueOrDefault(this.packagingHandlers, project.getPackaging(), this.defaultPackagingHandler);
        Repository outputRepository = packagingHandler.getOutputRepositoryFor(project);
        Iterable<Repository> additionalRepositories = packagingHandler.getAdditionalRepositoriesFor(project);
        Iterable<File> classPath = computeClassPath(project, knownModules);
        return new Module(outputRepository, classPath, additionalRepositories);
    }

    @Nonnull
    protected Iterable<File> computeClassPath(@Nonnull MavenProject project, @Nonnull Map<String, Module> knownModules) {
        String projectKey = getKeyFor(project);
        logger.debug("Computing class path for project {}...", projectKey);
        ArrayList<File> files = newArrayList();
        ScopeArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);
        for (Artifact dependency : project.getArtifacts()) {
            if (!artifactFilter.include(dependency)) {
                continue;
            }
            String dependencyKey = getKeyFor(dependency);
            Module knownModule = knownModules.get(dependencyKey);
            if (knownModule != null) {
                Repository outputRepository = knownModule.getOutputRepository();
                if (outputRepository == null) {
                    logger.debug("  No output available for {}; nothing added to the class path.", dependencyKey);
                } else {
                    files.add(outputRepository.getDirectory());
                    logger.debug("  Added project: {}", outputRepository.getDirectory());
                }
                continue;
            }
            if (!dependency.isResolved()) {
                ArtifactResolutionRequest request = new ArtifactResolutionRequest();
                request.setArtifact(dependency);
                ArtifactResolutionResult artifactResolutionResult = repositorySystem.resolve(request);
                if (!artifactResolutionResult.isSuccess()) {
                    logger.warn("  Failed to resolve [" + dependency + "]; some analyzers may not work properly.");
                    continue;
                }
            }
            File classPathElement = dependency.getFile();
            if (classPathElement == null) {
                logger.warn("  No valid path to [" + dependency + "] found; some analyzers may not work properly.");
                continue;
            }
            files.add(classPathElement);
            logger.debug("  Added artifact: {}", classPathElement);
        }
        return files;
    }

}
