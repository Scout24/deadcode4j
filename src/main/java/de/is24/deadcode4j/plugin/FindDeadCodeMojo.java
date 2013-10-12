package de.is24.deadcode4j.plugin;

import com.google.common.collect.Ordering;
import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeRepository;
import de.is24.deadcode4j.DeadCode;
import de.is24.deadcode4j.DeadCodeFinder;
import de.is24.deadcode4j.analyzer.ClassFileAnalyzer;
import de.is24.deadcode4j.analyzer.SpringXmlAnalyzer;
import de.is24.deadcode4j.analyzer.TldAnalyzer;
import de.is24.deadcode4j.analyzer.WebXmlAnalyzer;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.io.filefilter.FileFilterUtils.asFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;
import static org.apache.maven.plugin.MojoExecution.Source.CLI;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;

/**
 * Finds dead (i.e. unused) code.
 *
 * @since 1.0.0
 */
@Mojo(name = "find", aggregator = true, threadSafe = true, requiresProject = true)
@Execute(phase = PACKAGE)
public class FindDeadCodeMojo extends AbstractMojo {

    /**
     * Lists the "dead" classes that should be ignored.
     *
     * @since 1.0.1
     */
    @Parameter
    Set<String> classesToIgnore;
    @Component
    private MojoExecution mojoExecution;
    @Parameter(property = "reactorProjects", readonly = true)
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<MavenProject> reactorProjects;

    public void execute() throws MojoExecutionException {
        logWelcome();
        DeadCode deadCode = analyzeCode();
        log(deadCode);
        logGoodbye();
    }

    private void logWelcome() {
        if (CLI.equals(mojoExecution.getSource())) {
            getLog().info("Thanks for calling me! Let's see what I can do for you...");
        }
    }

    private DeadCode analyzeCode() throws MojoExecutionException {
        Set<Analyzer> analyzers = newHashSet(new ClassFileAnalyzer(), new SpringXmlAnalyzer(), new TldAnalyzer(), new WebXmlAnalyzer());
        DeadCodeFinder deadCodeFinder = new DeadCodeFinder(analyzers);
        return deadCodeFinder.findDeadCode(directoriesToAnalyze());
    }

    private CodeRepository[] directoriesToAnalyze() throws MojoExecutionException {
        List<CodeRepository> codeRepositories = newArrayList();
        for (MavenProject project : reactorProjects) {
            addDirectoriesOf(project, codeRepositories);
        }
        return codeRepositories.toArray(new CodeRepository[codeRepositories.size()]);
    }

    private void addDirectoriesOf(@Nonnull MavenProject project, @Nonnull List<CodeRepository> codeRepositories)
            throws MojoExecutionException {
        addOutputDirectory(project, codeRepositories);
        addWarSourceDirectory(project, codeRepositories);
    }

    private void addOutputDirectory(@Nonnull MavenProject project, @Nonnull List<CodeRepository> codeRepositories)
            throws MojoExecutionException {
        if ("pom".equalsIgnoreCase(project.getPackaging())) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Project [" + project.getGroupId() + ":" + project.getArtifactId() +
                        "] has pom packaging, so it is skipped.");
            }
            return;
        }
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        if (outputDirectory.exists()) {
            codeRepositories.add(new CodeRepository(outputDirectory));
            if (getLog().isDebugEnabled()) {
                getLog().debug("Going to analyze output directory [" + outputDirectory + "].");
            }
        } else {
            getLog().warn("The output directory of [" + project.getGroupId() + ":" + project.getArtifactId() +
                    "] does not exist - let's just hope the project simply has nothing to provide!");
        }
    }

    private void addWarSourceDirectory(@Nonnull MavenProject project, @Nonnull List<CodeRepository> codeRepositories)
            throws MojoExecutionException {
        if (!"war".equalsIgnoreCase(project.getPackaging()))
            return;
        if (getLog().isDebugEnabled()) {
            getLog().debug("Project [" + project.getGroupId() + ":" + project.getArtifactId() +
                    "] has war packaging, looking for webapp directory...");
        }
        Plugin plugin = project.getPlugin("org.apache.maven.plugins:maven-war-plugin");
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        Xpp3Dom webappDirectoryConfig = configuration == null ? null : configuration.getChild("webappDirectory");
        final File webappDirectory;
        if (webappDirectoryConfig != null) {
            webappDirectory = new File(webappDirectoryConfig.getValue());
            if (getLog().isDebugEnabled()) {
                getLog().debug("Going to analyze custom webapp directory [" + webappDirectory + "]");
            }
        } else {
            webappDirectory = new File(project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName());
            if (getLog().isDebugEnabled()) {
                getLog().debug("Going to analyze default webapp directory [" + webappDirectory + "]");
            }
        }
        if (!webappDirectory.exists()) {
            throw new MojoExecutionException("The webapp directory of [" + project.getId() +
                    "] does not exist - please make sure the project is packaged!");
        }
        final File directory = new File(webappDirectory, "WEB-INF");
        IOFileFilter fileFilter = notFileFilter(FileFilterUtils.orFileFilter(
                asFileFilter(new SubDirectoryFilter(directory, "classes")),
                asFileFilter(new SubDirectoryFilter(directory, "lib"))));
        codeRepositories.add(new CodeRepository(directory, fileFilter));
    }

    void log(DeadCode deadCode) {
        logAnalyzedClasses(deadCode.getAnalyzedClasses());

        Collection<String> deadClasses = newArrayList(deadCode.getDeadClasses());
        removeAndLogIgnoredClasses(deadClasses);

        logDeadClasses(deadClasses);
    }

    private void logAnalyzedClasses(Collection<String> analyzedClasses) {
        getLog().info("Analyzed " + analyzedClasses.size() + " class(es).");
    }

    private void removeAndLogIgnoredClasses(Collection<String> deadClasses) {
        if (this.classesToIgnore == null)
            return;

        final int numberOfUnusedClasses = deadClasses.size();
        for (String ignoredClass : this.classesToIgnore) {
            if (!deadClasses.remove(ignoredClass)) {
                getLog().warn("Class [" + ignoredClass + "] should be ignored, but is not dead. You should remove the configuration entry.");
            }
        }

        int removedClasses = numberOfUnusedClasses - deadClasses.size();
        if (removedClasses != 0) {
            getLog().info("Ignoring " + removedClasses + " class(es) which seem(s) to be unused.");
        }
    }

    private void logDeadClasses(Collection<String> deadClasses) {
        Log log = getLog();
        int numberOfDeadClasses = deadClasses.size();
        if (numberOfDeadClasses == 0) {
            log.info("No unused classes found. Rejoice!");
            return;
        }
        log.warn("Found " + numberOfDeadClasses + " unused class(es):");
        for (String unusedClass : Ordering.natural().sortedCopy(deadClasses)) {
            log.warn("  " + unusedClass);
        }
    }

    private void logGoodbye() {
        if (CLI.equals(mojoExecution.getSource())) {
            getLog().info("Expected something different? Don't like the results? " +
                    "Hop on over to https://github.com/ImmobilienScout24/deadcode4j to learn more!");
        }
    }

}
