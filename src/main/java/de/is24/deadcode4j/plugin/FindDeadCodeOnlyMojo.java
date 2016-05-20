package de.is24.deadcode4j.plugin;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import de.is24.deadcode4j.*;
import de.is24.deadcode4j.analyzer.*;
import de.is24.maven.UpdateChecker;
import de.is24.maven.slf4j.AbstractSlf4jMojo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static de.is24.deadcode4j.Utils.getKeyFor;
import static de.is24.deadcode4j.Utils.toKey;
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
/* Technical Note: The mojo should wire everything together; i.e. new instance creation should only happen here. */
@Mojo(name = "find-only",
        aggregator = true,
        requiresProject = true,
        requiresDependencyCollection = COMPILE,
        threadSafe = true)
public class FindDeadCodeOnlyMojo extends AbstractSlf4jMojo {

    /**
     * Lists the fqcn of the annotations marking a class as being "live code".
     *
     * @since 1.3
     */
    @Parameter
    @SuppressWarnings("PMD.ImmutableField")
    private Set<String> annotationsMarkingLiveCode = emptySet();
    /**
     * Lists the "dead" classes that should be ignored.
     *
     * @since 1.0.1
     */
    @Parameter
    @SuppressWarnings("PMD.ImmutableField")
    private Set<String> classesToIgnore = emptySet();
    /**
     * Lists the custom XML analysis configurations to set up.
     * Have a look at https://github.com/ImmobilienScout24/deadcode4j to learn how to configure a custom XML analyzer.
     *
     * @since 1.3
     */
    @Parameter
    @SuppressWarnings("PMD.ImmutableField")
    private List<CustomXml> customXmls = emptyList();
    /**
     * Mark all classes with a main method as being "live code".
     *
     * @since 2.0.0
     */
    @Parameter
    @SuppressWarnings("PMD.ImmutableField")
    private boolean ignoreMainClasses = false;
    /**
     * Lists the fqcn of the interfaces marking a class as being "live code".
     *
     * @since 1.4
     */
    @Parameter
    @SuppressWarnings("PMD.ImmutableField")
    private Set<String> interfacesMarkingLiveCode = emptySet();
    @Component
    private MavenProject project;
    /**
     * Lists the modules that should not be analyzed.
     *
     * @since 1.4
     */
    @Parameter
    @SuppressWarnings("PMD.ImmutableField")
    private List<String> modulesToSkip = emptyList();
    @Component
    private MojoExecution mojoExecution;
    @Parameter(property = "reactorProjects", readonly = true)
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<MavenProject> reactorProjects;
    @Component
    private RepositorySystem repositorySystem;
    /**
     * Skip sending usage statistics.<br/>
     * If set to {@code false}, statistics will be sent.<br/>
     * If nothing is configured and running in interactive mode (NOT using the -B flag), the user is requested to allow sending the usage statistics.<br/>
     * Note that this step is skipped if Maven is running in offline mode (using the -o flag).
     *
     * @since 2.0.0
     */
    @Parameter(property = "deadcode4j.skipSendingStatistics")
    private Boolean skipSendingUsageStatistics;
    /**
     * Skip the update check performed at startup.<br/>
     * Note that this step is skipped if Maven is running in offline mode (using the -o flag).
     *
     * @since 2.0.0
     */
    @Parameter(property = "deadcode4j.skipUpdate")
    @SuppressWarnings("PMD.ImmutableField")
    private boolean skipUpdateCheck = false;
    /**
     * Lists the fqcn of the classes marking a direct subclass as being "live code".
     *
     * @since 1.4
     */
    @Parameter
    @SuppressWarnings("PMD.ImmutableField")
    private Set<String> superClassesMarkingLiveCode = emptySet();
    @Component
    private UpdateChecker updateChecker;
    /**
     * The comment to send along with the usage statistics.<br/>
     * State a testimonial, refer to your project, provide a way to contact you, request a feature, ...
     *
     * @see #skipSendingUsageStatistics
     * @since 2.0.0
     */
    @Parameter(property = "deadcode4j.statisticsComment")
    private String usageStatisticsComment;
    @Component
    private UsageStatisticsManager usageStatisticsManager;

    public void doExecute() throws MojoExecutionException {
        try {
            checkForUpdate();
            logWelcome();
            DeadCode deadCode = analyzeCode();
            log(deadCode);
            logGoodbye();
            sendStatistics(deadCode);
        } catch (RuntimeException rE) {
            getLog().error("An unexpected exception occurred. " +
                    "Please consider reporting an issue at https://github.com/ImmobilienScout24/deadcode4j/issues", rE);
            throw rE;
        }
    }

    private void sendStatistics(DeadCode deadCode) {
        UsageStatisticsManager.DeadCodeStatistics deadCodeStatistics = new UsageStatisticsManager.DeadCodeStatistics(
                this.skipSendingUsageStatistics,
                this.usageStatisticsComment);
        deadCodeStatistics.config_ignoreMainClasses = this.ignoreMainClasses;
        deadCodeStatistics.config_numberOfClassesToIgnore = this.classesToIgnore.size();
        deadCodeStatistics.config_numberOfCustomAnnotations = this.annotationsMarkingLiveCode.size();
        deadCodeStatistics.config_numberOfCustomInterfaces = this.interfacesMarkingLiveCode.size();
        deadCodeStatistics.config_numberOfCustomSuperclasses = this.superClassesMarkingLiveCode.size();
        deadCodeStatistics.config_numberOfCustomXmlDefinitions = this.customXmls.size();
        deadCodeStatistics.config_numberOfModulesToSkip = this.modulesToSkip.size();
        deadCodeStatistics.config_skipUpdateCheck = this.skipUpdateCheck;

        deadCodeStatistics.numberOfAnalyzedClasses = deadCode.getAnalyzedClasses().size();
        deadCodeStatistics.numberOfAnalyzedModules = this.reactorProjects.size();
        deadCodeStatistics.numberOfDeadClassesFound = deadCode.getDeadClasses().size();

        this.usageStatisticsManager.sendUsageStatistics(deadCodeStatistics);
    }

    private void checkForUpdate() {
        if (skipUpdateCheck) {
            return;
        }
        Optional<ArtifactVersion> mostRecentVersion = updateChecker.checkForUpdate(mojoExecution);
        if (mostRecentVersion.isPresent()) {
            getLog().warn("The new version [" + mostRecentVersion.get() +
                    "] is available; consider updating for better analysis results!");
        }
    }

    private void logWelcome() {
        if (mojoExecution != null && CLI.equals(mojoExecution.getSource())) {
            getLog().info("Thanks for calling me! Let's see what I can do for you...");
        }
    }

    private DeadCode analyzeCode() throws MojoExecutionException {
        Set<Analyzer> analyzers = Sets.<Analyzer>newHashSet(
                new AopXmlAnalyzer(),
                new ApacheTilesAnalyzer(),
                new CastorClassesAnalyzer(),
                new ClassDependencyAnalyzer(),
                new FacesConfigXmlAnalyzer(),
                new HibernateAnnotationsAnalyzer(),
                new JeeAnnotationsAnalyzer(),
                new JerseyWebXmlAnalyzer(),
                new JettyXmlAnalyzer(),
                new LogbackXmlAnalyzer(),
                new ReferenceToConstantsAnalyzer(),
                new ServletContainerInitializerAnalyzer(),
                new SpringAnnotationsAnalyzer(),
                new SpringDataCustomRepositoriesAnalyzer(),
                new SpringNamespaceHandlerAnalyzer(),
                new SpringWebApplicationInitializerAnalyzer(),
                new SpringWebFlowAnalyzer(),
                new SpringWebXmlAnalyzer(),
                new SpringXmlAnalyzer(),
                new TldAnalyzer(),
                new TypeErasureAnalyzer(),
                new WebXmlAnalyzer(),
                new WsddAnalyzer());
        DeadCodeComputer deadCodeComputer = new DeadCodeComputer();
        addCustomAnnotationsAnalyzerIfConfigured(analyzers);
        addCustomInterfacesAnalyzerIfConfigured(analyzers);
        addCustomSuperClassesAnalyzerIfConfigured(analyzers);
        addCustomXmlAnalyzerIfConfigured(analyzers);
        addIgnoreClassesAnalyzerIfConfigured(deadCodeComputer, analyzers);
        addMainClassAnalyzerIfConfigured(analyzers);
        DeadCodeFinder deadCodeFinder = new DeadCodeFinder(deadCodeComputer, analyzers);
        return deadCodeFinder.findDeadCode(gatherModules());
    }

    private void addCustomAnnotationsAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (annotationsMarkingLiveCode.isEmpty()) {
            return;
        }
        analyzers.add(new CustomAnnotationsAnalyzer(annotationsMarkingLiveCode));
        getLog().info("Treating classes annotated with any of " + annotationsMarkingLiveCode + " as live code.");
    }

    private void addCustomInterfacesAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (interfacesMarkingLiveCode.isEmpty()) {
            return;
        }
        analyzers.add(new CustomInterfacesAnalyzer(interfacesMarkingLiveCode));
        getLog().info("Treating classes implementing any of " + interfacesMarkingLiveCode + " as live code.");
    }

    private void addCustomSuperClassesAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (superClassesMarkingLiveCode.isEmpty()) {
            return;
        }
        analyzers.add(new CustomSuperClassAnalyzer(superClassesMarkingLiveCode));
        getLog().info("Treating classes being subclasses of any of " + superClassesMarkingLiveCode + " as live code.");
    }

    private void addCustomXmlAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (customXmls.isEmpty()) {
            return;
        }
        for (CustomXml customXml : customXmls) {
            CustomXmlAnalyzer customXmlAnalyzer = new CustomXmlAnalyzer(customXml.getEndOfFileName(), customXml.getRootElement());
            checkArgument(!customXml.getXPaths().isEmpty(), "At least one entry for [xPaths] must be set!");
            for (String xPath : customXml.getXPaths()) {
                customXmlAnalyzer.registerXPath(xPath);
                String rootPath = customXml.getRootElement() == null ? "" : "/" + customXml.getRootElement();
                getLog().info("Treating classes found at [" + rootPath + "//" + xPath + "] in [" + customXml.getEndOfFileName() + "] files as live code.");
            }
            analyzers.add(customXmlAnalyzer);
        }
    }

    private void addIgnoreClassesAnalyzerIfConfigured(DeadCodeComputer deadCodeComputer, Set<Analyzer> analyzers) {
        if (classesToIgnore.isEmpty()) {
            return;
        }
        analyzers.add(new IgnoreClassesAnalyzer(deadCodeComputer, classesToIgnore));
    }

    private void addMainClassAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (!ignoreMainClasses) {
            return;
        }
        analyzers.add(new MainClassAnalyzer());
        getLog().info("Treating classes with a main method as live code.");
    }

    private Iterable<Module> gatherModules() throws MojoExecutionException {
        ModuleGenerator moduleGenerator = new ModuleGenerator(this.repositorySystem);
        return moduleGenerator.getModulesFor(getProjectsToAnalyze());
    }

    private Collection<MavenProject> getProjectsToAnalyze() {
        if (this.modulesToSkip.isEmpty()) {
            return this.reactorProjects;
        }
        ArrayList<String> unknownModules = newArrayList(this.modulesToSkip);
        int baseDirPathIndex = project.getBasedir().getAbsolutePath().length() + 1;
        ArrayList<MavenProject> mavenProjects = newArrayList(this.reactorProjects);
        for (MavenProject mavenProject : this.reactorProjects) {
            if (project.equals(mavenProject)) {
                continue;
            }
            String projectPath = mavenProject.getBasedir().getAbsolutePath();
            String modulePath = projectPath.substring(baseDirPathIndex);
            if (this.modulesToSkip.contains(modulePath)) {
                unknownModules.remove(modulePath);
                getLog().info("Project [" + getKeyFor(mavenProject) + "] will be skipped.");
                mavenProjects.remove(mavenProject);
                List<MavenProject> collectedProjects = mavenProject.getCollectedProjects();
                if (!collectedProjects.isEmpty()) {
                    getLog().info("  Aggregated Projects " + transform(collectedProjects, toKey()) + " will be skipped.");
                    mavenProjects.removeAll(collectedProjects);
                }
            }
        }
        for (String unknownModule : unknownModules) {
            getLog().warn("Module [" + unknownModule + "] should be skipped, but does not exist. You should remove the configuration entry.");
        }

        return mavenProjects;
    }

    private void log(DeadCode deadCode) {
        new DeadCodeLogger(getLog()).log(deadCode);
    }

    private void logGoodbye() {
        if (mojoExecution != null && CLI.equals(mojoExecution.getSource())) {
            getLog().info("Expected something different? Don't like the results? " +
                    "Hop on over to https://github.com/ImmobilienScout24/deadcode4j to learn more!");
        }
    }

}
