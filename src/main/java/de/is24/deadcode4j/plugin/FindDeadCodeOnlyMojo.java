package de.is24.deadcode4j.plugin;

import com.google.common.collect.Sets;
import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.DeadCode;
import de.is24.deadcode4j.DeadCodeFinder;
import de.is24.deadcode4j.Module;
import de.is24.deadcode4j.analyzer.*;
import de.is24.maven.slf4j.AbstractSlf4jMojo;
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
        Set<Analyzer> analyzers = Sets.<Analyzer>newHashSet(
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
                new TypeErasureAnalyzer(),
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
        getLog().info("Treating classes implementing any of " + interfacesMarkingLiveCode + " as live code.");
    }

    private void addCustomSuperClassesAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (superClassesMarkingLiveCode.isEmpty())
            return;
        analyzers.add(new CustomSuperClassAnalyzer(superClassesMarkingLiveCode));
        getLog().info("Treating classes being subclasses of any of " + superClassesMarkingLiveCode + " as live code.");
    }

    private void addCustomXmlAnalyzerIfConfigured(Set<Analyzer> analyzers) {
        if (customXmls.isEmpty())
            return;
        for (CustomXml customXml : customXmls) {
            CustomXmlAnalyzer customXmlAnalyzer = new CustomXmlAnalyzer(customXml.getEndOfFileName(), customXml.getRootElement());
            checkArgument(!customXml.getXPaths().isEmpty(), "At least one entry for [xPaths] must be set!");
            for (String xPath : customXml.getXPaths()) {
                customXmlAnalyzer.registerXPath(xPath);
                getLog().info("Treating classes found at [/" + customXml.getRootElement() + "//" + xPath + "] in [" + customXml.getEndOfFileName() + "] files as live code.");
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
                if (collectedProjects.size() > 0) {
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
        new DeadCodeLogger(getLog()).log(deadCode, this.classesToIgnore);
    }

    private void logGoodbye() {
        if (mojoExecution != null && CLI.equals(mojoExecution.getSource())) {
            getLog().info("Expected something different? Don't like the results? " +
                    "Hop on over to https://github.com/ImmobilienScout24/deadcode4j to learn more!");
        }
    }

}
