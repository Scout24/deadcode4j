package de.is24.deadcode4j.plugin;

import com.google.common.collect.Sets;
import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeRepository;
import de.is24.deadcode4j.DeadCode;
import de.is24.deadcode4j.DeadCodeFinder;
import de.is24.deadcode4j.analyzer.*;
import de.is24.maven.slf4j.AbstractSlf4jMojo;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newHashMap;
import static de.is24.deadcode4j.Utils.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.apache.commons.io.filefilter.FileFilterUtils.asFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;
import static org.apache.maven.plugin.MojoExecution.Source.CLI;

/**
 * Finds dead (i.e. unused) code. In contrast to <code>find</code>, no phase is executed.
 *
 * @see FindDeadCodeMojo
 * @since 1.5
 */
@Mojo(name = "find-only", aggregator = true, threadSafe = true, requiresProject = true)
@SuppressWarnings("PMD.TooManyStaticImports")
public class FindDeadCodeOnlyMojo extends AbstractSlf4jMojo {

    private final Map<String, PackagingHandler> packagingHandlers = newHashMap();
    private final PackagingHandler defaultPackagingHandler = new DefaultPackagingHandler();
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

    public FindDeadCodeOnlyMojo() {
        packagingHandlers.put("pom", new PomPackagingHandler());
        packagingHandlers.put("war", new WarPackagingHandler());
    }

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
        if (CLI.equals(mojoExecution.getSource())) {
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
        return deadCodeFinder.findDeadCode(gatherCodeRepositories());
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

    private Iterable<CodeRepository> gatherCodeRepositories() throws MojoExecutionException {
        List<CodeRepository> codeRepositories = newArrayList();
        for (MavenProject project : getProjectsToAnalyze()) {
            addIfNonNull(codeRepositories, getCodeRepositoryFor(project));
        }
        return codeRepositories;
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
                    getLog().info("  Aggregated Projects " + transform(collectedProjects, toKey()) + " will be skipped.");
                    mavenProjects.removeAll(collectedProjects);
                }
            }
        }

        return mavenProjects;
    }

    @Nullable
    private CodeRepository getCodeRepositoryFor(@Nonnull MavenProject project) throws MojoExecutionException {
        PackagingHandler packagingHandler =
                getValueOrDefault(this.packagingHandlers, project.getPackaging(), this.defaultPackagingHandler);
        return packagingHandler.getCodeRepositoryFor(project);
    }

    private void log(DeadCode deadCode) {
        new DeadCodeLogger(getLog()).log(deadCode, this.classesToIgnore);
    }

    private void logGoodbye() {
        if (CLI.equals(mojoExecution.getSource())) {
            getLog().info("Expected something different? Don't like the results? " +
                    "Hop on over to https://github.com/ImmobilienScout24/deadcode4j to learn more!");
        }
    }

    private interface PackagingHandler {
        @Nullable
        CodeRepository getCodeRepositoryFor(@Nonnull MavenProject project) throws MojoExecutionException;
    }

    private class PomPackagingHandler implements PackagingHandler {
        @Override
        @Nullable
        public CodeRepository getCodeRepositoryFor(@Nonnull MavenProject project) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Project " + getKeyFor(project) + " has pom packaging, so it is skipped.");
            }
            return null;
        }
    }

    private class WarPackagingHandler implements PackagingHandler {
        @Override
        @Nullable
        public CodeRepository getCodeRepositoryFor(@Nonnull MavenProject project) throws MojoExecutionException {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Project " + getKeyFor(project) + " has war packaging, looking for webapp directory...");
            }
            Plugin plugin = project.getPlugin("org.apache.maven.plugins:maven-war-plugin");
            Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
            Xpp3Dom webappDirectoryConfig = configuration == null ? null : configuration.getChild("webappDirectory");
            final File webappDirectory;
            if (webappDirectoryConfig != null) {
                webappDirectory = new File(webappDirectoryConfig.getValue());
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Found custom webapp directory [" + webappDirectory + "].");
                }
            } else {
                webappDirectory = new File(project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName());
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Using default webapp directory [" + webappDirectory + "].");
                }
            }
            if (!webappDirectory.exists()) {
                throw new MojoExecutionException("The webapp directory of " + getKeyFor(project) +
                        " does not exist - please make sure the project is packaged!");
            }
            final File directory = new File(webappDirectory, "WEB-INF");
            IOFileFilter fileFilter = notFileFilter(asFileFilter(new SubDirectoryFilter(directory, "lib")));
            return new CodeRepository(directory, fileFilter);
        }
    }

    private class DefaultPackagingHandler implements PackagingHandler {
        @Override
        @Nullable
        public CodeRepository getCodeRepositoryFor(@Nonnull MavenProject project) {
            File outputDirectory = new File(project.getBuild().getOutputDirectory());
            if (!outputDirectory.exists()) {
                getLog().warn("The output directory of " + getKeyFor(project) +
                        " does not exist - assuming the project simply has nothing to provide!");
                return null;
            }
            if (getLog().isDebugEnabled()) {
                getLog().debug("Going to analyze output directory [" + outputDirectory + "].");
            }
            return new CodeRepository(outputDirectory);
        }
    }

}
