package de.is24.deadcode4j.plugin;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import de.is24.deadcode4j.Module;
import de.is24.deadcode4j.Repository;
import de.is24.deadcode4j.Resource;
import de.is24.deadcode4j.plugin.packaginghandler.DefaultPackagingHandler;
import de.is24.deadcode4j.plugin.packaginghandler.PackagingHandler;
import de.is24.deadcode4j.plugin.packaginghandler.PomPackagingHandler;
import de.is24.deadcode4j.plugin.packaginghandler.WarPackagingHandler;
import de.is24.guava.NonNullFunction;
import de.is24.guava.NonNullFunctions;
import de.is24.guava.SequentialLoadingCache;
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
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static de.is24.deadcode4j.Utils.*;

/**
 * Calculates the modules for the given maven projects.
 *
 * @see #getModulesFor(Iterable)
 * @since 2.0.0
 */
class ModuleGenerator {

    @Nonnull
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Nonnull
    private final PackagingHandler defaultPackagingHandler = new DefaultPackagingHandler();
    @Nonnull
    private final Map<String, PackagingHandler> packagingHandlers = newHashMap();
    @Nonnull
    private final LoadingCache<Artifact, Optional<File>> artifactResolverCache;

    /**
     * Creates a new <code>ModuleGenerator</code>.
     *
     * @param repositorySystem the given <code>RepositorySystem</code> is required to resolve the class path of the
     *                         examined maven projects
     * @since 2.0.0
     */
    public ModuleGenerator(@Nonnull final RepositorySystem repositorySystem) {
        packagingHandlers.put("pom", new PomPackagingHandler());
        packagingHandlers.put("war", new WarPackagingHandler());
        artifactResolverCache = new SequentialLoadingCache<Artifact, File>(NonNullFunctions.toFunction(new NonNullFunction<Artifact, Optional<File>>() {
            @Nonnull
            @Override
            public Optional<File> apply(@Nonnull Artifact input) {
                if (!input.isResolved()) {
                    ArtifactResolutionRequest request = new ArtifactResolutionRequest();
                    request.setResolveRoot(true);
                    request.setResolveTransitively(false);
                    request.setArtifact(input);
                    ArtifactResolutionResult artifactResolutionResult = repositorySystem.resolve(request);
                    if (!artifactResolutionResult.isSuccess()) {
                        logger.warn("  Failed to resolve [{}]; some analyzers may not work properly.", getVersionedKeyFor(input));
                        return absent();
                    }
                }
                File classPathElement = input.getFile();
                if (classPathElement == null) {
                    logger.warn("  No valid path to [{}] found; some analyzers may not work properly.", getVersionedKeyFor(input));
                    return absent();
                }
                return of(classPathElement);
            }
        }));
    }

    /**
     * Attempts to create a <code>Module</code> for each given maven project. A project providing no repository will be
     * silently ignored.
     *
     * @param projects the <code>MavenProject</code>s to transform; the projects MUST be ordered such that project
     *                 <i>A</i> is listed before project <i>B</i> if <i>B</i> depends on <i>A</i>
     * @throws MojoExecutionException if calculating the repositories fails
     * @since 2.0.0
     */
    @Nonnull
    public Iterable<Module> getModulesFor(@Nonnull Iterable<MavenProject> projects) throws MojoExecutionException {
        Map<String, Module> knownModules = newHashMap();
        for (MavenProject project : projects) {
            Module module = getModuleFor(project, knownModules);
            knownModules.put(module.getModuleId(), module);
            logger.debug("Added [{}] for [{}].", module, project);
        }
        return knownModules.values();
    }

    @Nonnull
    private Module getModuleFor(
            @Nonnull MavenProject project,
            @Nonnull Map<String, Module> knownModules) throws MojoExecutionException {
        String projectId = getKeyFor(project);
        String encoding = emptyToNull(project.getProperties().getProperty("project.build.sourceEncoding"));
        if (encoding == null) {
            logger.warn("No encoding set for [{}]! Parsing source files may cause issues.", projectId);
        }
        PackagingHandler packagingHandler =
                getValueOrDefault(this.packagingHandlers, project.getPackaging(), this.defaultPackagingHandler);
        Repository outputRepository = packagingHandler.getOutputRepositoryFor(project);
        Iterable<Repository> additionalRepositories = packagingHandler.getAdditionalRepositoriesFor(project);
        Collection<Resource> dependencies = computeDependencies(project, knownModules);
        return new Module(projectId, encoding, dependencies, outputRepository, additionalRepositories);
    }

    @Nonnull
    protected Collection<Resource> computeDependencies(
            @Nonnull MavenProject project,
            @Nonnull Map<String, Module> knownModules) {
        logger.debug("Gathering dependencies for project [{}]...", getKeyFor(project));
        List<Resource> dependencies = newArrayList();
        for (Artifact dependency : filter(project.getArtifacts(), artifactsWithCompileScope())) {
            if (addKnownArtifact(dependencies, dependency, knownModules)) {
                continue;
            }
            final Optional<File> artifactPath = this.artifactResolverCache.getUnchecked(dependency);
            if (artifactPath.isPresent()) {
                final File classPathElement = artifactPath.get();
                dependencies.add(Resource.of(classPathElement));
                logger.debug("  Added artifact: [{}]", classPathElement);
            }
        }
        logger.debug("[{}] dependencies found for [{}].", dependencies.size(), getKeyFor(project));
        return dependencies;
    }

    private Predicate<Artifact> artifactsWithCompileScope() {
        return new Predicate<Artifact>() {
            private ScopeArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);

            @Override
            public boolean apply(@Nullable Artifact input) {
                return input != null && artifactFilter.include(input);
            }
        };
    }

    private boolean addKnownArtifact(
            @Nonnull List<Resource> resources,
            @Nonnull Artifact artifact,
            @Nonnull Map<String, Module> knownModules) {
        String dependencyKey = getKeyFor(artifact);
        Module knownModule = knownModules.get(dependencyKey);
        if (knownModule == null) {
            return false;
        }
        resources.add(Resource.of(knownModule));
        logger.debug("  Added project: [{}]", knownModule);
        return true;
    }

}
