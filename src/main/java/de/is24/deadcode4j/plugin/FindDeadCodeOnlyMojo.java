package de.is24.deadcode4j.plugin;

import com.google.common.collect.Sets;
import de.is24.deadcode4j.*;
import de.is24.deadcode4j.analyzer.*;
import de.is24.deadcode4j.plugin.packaginghandler.DefaultPackagingHandler;
import de.is24.deadcode4j.plugin.packaginghandler.PackagingHandler;
import de.is24.deadcode4j.plugin.packaginghandler.PomPackagingHandler;
import de.is24.deadcode4j.plugin.packaginghandler.WarPackagingHandler;
import de.is24.maven.slf4j.AbstractSlf4jMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newHashMap;
import static de.is24.deadcode4j.Utils.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.apache.maven.plugin.MojoExecution.Source.CLI;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

/**
 * Finds dead (i.e. unused) code. In contrast to <code>find</code>, no phase is executed.
 *
 * @see FindDeadCodeMojo
 * @since 1.5
 */
@Mojo(name = "find-only",
        aggregator = true,
        requiresProject = true,
        requiresDependencyCollection = COMPILE,
        threadSafe = true)
@SuppressWarnings("PMD.TooManyStaticImports")
public class FindDeadCodeOnlyMojo extends AbstractSlf4jMojo {

    /**
     * Lists the fqcn of the annotations marking a class as being "live code".
     *
     * @since 1.3
     */
    @Parameter
    private Set<String> annotationsMarkingLiveCode = emptySet();
    /**
     * Lists the "dead" classes that should be ignored.
     *
     * @since 1.0.1
     */
    @Parameter
    private Set<String> classesToIgnore = emptySet();
    /**
     * Lists the custom XML analysis configurations to set up.
     * Have a look at https://github.com/ImmobilienScout24/deadcode4j to learn how to configure a custom XML analyzer.
     *
     * @since 1.3
     */
    @Parameter
    private List<CustomXml> customXmls = emptyList();
    /**
     * Lists the fqcn of the interfaces marking a class as being "live code".
     *
     * @since 1.4
     */
    @Parameter
    private Set<String> interfacesMarkingLiveCode = emptySet();
    @Component
    private MavenProject project;
    /**
     * Lists the modules that should not be analyzed.
     *
     * @since 1.4
     */
    @Parameter
    private List<String> modulesToSkip = emptyList();
    @Component
    private MojoExecution mojoExecution;
    @Parameter(property = "reactorProjects", readonly = true)
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<MavenProject> reactorProjects;
    @Component
    private RepositorySystem repositorySystem;
    /**
     * Lists the fqcn of the classes marking a direct subclass as being "live code".
     *
     * @since 1.4
     */
    @Parameter
    private Set<String> superClassesMarkingLiveCode = emptySet();
    /**
     * This parameter only exists to have a generated <code>help</code> goal. It is not used at all.
     *
     * @since 1.4.1
     * @deprecated this value is ignored
     */
    @Deprecated
    @Parameter(defaultValue = "fooBar")
    @SuppressWarnings("unused")
    private String workAroundForHelpMojo;

    public void doExecute() throws MojoExecutionException {
        try {
            logWelcome();
            DeadCode deadCode = analyzeCode();
            log(deadCode);
            logGoodbye();
        } catch (RuntimeException rE) {
            getLog().error("An unexpected exception occurred." +
                    "Please consider reporting an issue at https://github.com/ImmobilienScout24/deadcode4j/issues", rE);
            throw rE;
        }
    }

    private void logWelcome() {
        if (mojoExecution != null && CLI.equals(mojoExecution.getSource())) {
            getLog().info("Thanks for calling me! Let's see what I can do for you...");
        }
    }

    private DeadCode analyzeCode() throws MojoExecutionException {
        Set<Analyzer> analyzers = Sets.newHashSet(
                new AopXmlAnalyzer(),
                new ApacheTilesAnalyzer(),
                new CastorClassesAnalyzer(),
                new ClassDependencyAnalyzer(),
                new FacesConfigXmlAnalyzer(),
                new HibernateAnnotationsAnalyzer(),
                new JeeAnnotationsAnalyzer(),
                new ServletContainerInitializerAnalyzer(),
                new SpringAnnotationsAnalyzer(),
                new SpringNamespaceHandlerAnalyzer(),
                new SpringWebApplicationInitializerAnalyzer(),
                new SpringWebFlowAnalyzer(),
                new SpringWebXmlAnalyzer(),
                new SpringXmlAnalyzer(),
                new TldAnalyzer(),
                new WebXmlAnalyzer(),
                new WsddAnalyzer());
        addCustomAnnotationsAnalyzerIfConfigured(analyzers);
        addCustomInterfacesAnalyzerIfConfigured(analyzers);
        addCustomSuperClassesAnalyzerIfConfigured(analyzers);
        addCustomXmlAnalyzerIfConfigured(analyzers);
        DeadCodeFinder deadCodeFinder = new DeadCodeFinder(analyzers);
        return deadCodeFinder.findDeadCode(gatherModules());
    }

    private void addCustomAnnotationsAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (annotationsMarkingLiveCode.isEmpty())
            return;
        analyzers.add(new CustomAnnotationsAnalyzer(annotationsMarkingLiveCode));
        getLog().info("Treating classes annotated with any of " + annotationsMarkingLiveCode + " as live code.");
    }

    private void addCustomInterfacesAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (interfacesMarkingLiveCode.isEmpty())
            return;
        analyzers.add(new CustomInterfacesAnalyzer(interfacesMarkingLiveCode));
        getLog().info("Treating classes explicitly implementing any of " + interfacesMarkingLiveCode + " as live code.");
    }

    private void addCustomSuperClassesAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (superClassesMarkingLiveCode.isEmpty())
            return;
        analyzers.add(new CustomSuperClassAnalyzer(superClassesMarkingLiveCode));
        getLog().info("Treating classes being direct subclasses of any of " + superClassesMarkingLiveCode + " as live code.");
    }

    private void addCustomXmlAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (customXmls.isEmpty())
            return;
        for (CustomXml customXml : customXmls) {
            CustomXmlAnalyzer customXmlAnalyzer = new CustomXmlAnalyzer(customXml.getEndOfFileName(), customXml.getRootElement());
            checkArgument(!customXml.getXPaths().isEmpty(), "At least one entry for [xPaths] must be set!");
            for (String xPath : customXml.getXPaths()) {
                customXmlAnalyzer.registerXPath(xPath);
                getLog().info("Treating classes found at [/" + customXml.getRootElement() + "//" + xPath + "] as live code.");
            }
            analyzers.add(customXmlAnalyzer);
        }
    }

    private Iterable<Module> gatherModules() throws MojoExecutionException {
        ModuleGenerator moduleGenerator = new ModuleGenerator(this.repositorySystem);
        return moduleGenerator.getModulesFor(getProjectsToAnalyze());
    }

    private Collection<MavenProject> getProjectsToAnalyze() {
        if (this.modulesToSkip.isEmpty()) {
            return this.reactorProjects;
        }
        getLog().info("Skipping modules " + this.modulesToSkip + ":");
        int baseDirPathIndex = project.getBasedir().getAbsolutePath().length() + 1;
        ArrayList<MavenProject> mavenProjects = newArrayList(this.reactorProjects);
        for (MavenProject mavenProject : this.reactorProjects) {
            if (project.equals(mavenProject)) {
                continue;
            }
            String projectPath = mavenProject.getBasedir().getAbsolutePath();
            String modulePath = projectPath.substring(baseDirPathIndex);
            if (this.modulesToSkip.contains(modulePath)) {
                getLog().info("  Project [" + getKeyFor(mavenProject) + "] will be skipped.");
                mavenProjects.remove(mavenProject);
                List<MavenProject> collectedProjects = mavenProject.getCollectedProjects();
                if (collectedProjects.size() > 0) {
                    getLog().info("    Aggregated Projects " + transform(collectedProjects, toKey()) + " will be skipped.");
                    mavenProjects.removeAll(collectedProjects);
                }
            }
        }

        return mavenProjects;
    }

    private void log(DeadCode deadCode) {
        new DeadCodeLogger(getLog()).log(deadCode, this.classesToIgnore);
    }

    private void logGoodbye() {
        if (mojoExecution != null && CLI.equals(mojoExecution.getSource())) {
            getLog().info("Expected something different? Don't like the results? " +
                    "Hop on over to https://github.com/ImmobilienScout24/deadcode4j to learn more!");
        }
    }

    /**
     * Calculates the module for a given maven project.
     *
     * @since 1.6
     */
    private static class ModuleGenerator {
        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final PackagingHandler defaultPackagingHandler = new DefaultPackagingHandler();
        private final Map<String, PackagingHandler> packagingHandlers = newHashMap();
        private final RepositorySystem repositorySystem;

        private ModuleGenerator(@Nonnull RepositorySystem repositorySystem) {
            this.repositorySystem = repositorySystem;
            packagingHandlers.put("pom", new PomPackagingHandler());
            packagingHandlers.put("war", new WarPackagingHandler());
        }


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

        protected Iterable<File> computeClassPath(MavenProject project, Map<String, Module> knownModules) {
            String projectKey = getKeyFor(project);
            logger.debug("Computing class path for project {}:", projectKey);
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
                        logger.debug("No output available for {}; nothing added to the class path.", dependencyKey);
                    } else {
                        files.add(outputRepository.getDirectory());
                        logger.debug("Added project: {}", outputRepository.getDirectory());
                    }
                    continue;
                }
                if (!dependency.isResolved()) {
                    ArtifactResolutionRequest request = new ArtifactResolutionRequest();
                    request.setArtifact(dependency);
                    ArtifactResolutionResult artifactResolutionResult = repositorySystem.resolve(request);
                    if (!artifactResolutionResult.isSuccess()) {
                        logger.warn("Failed to resolve [" + dependency + "]; some analyzers may not work properly.");
                        continue;
                    }
                }
                File classPathElement = dependency.getFile();
                if (classPathElement == null) {
                    logger.warn("No valid path to [" + dependency + "] found; some analyzers may not work properly.");
                    continue;
                }
                files.add(classPathElement);
                logger.debug("Added artifact: {}", classPathElement);
            }
            return files;
        }

    }

}
