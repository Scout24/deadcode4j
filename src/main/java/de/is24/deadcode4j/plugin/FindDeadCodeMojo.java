package de.is24.deadcode4j.plugin;

import com.google.common.collect.Ordering;
import de.is24.deadcode4j.DeadCode;
import de.is24.deadcode4j.DeadCodeFinder;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.maven.plugin.MojoExecution.Source.CLI;
import static org.apache.maven.plugins.annotations.LifecyclePhase.COMPILE;

/**
 * Finds dead (i.e. unused) code.
 *
 * @since 1.0.0
 */
@Mojo(name = "find", threadSafe = true)
@Execute(phase = COMPILE)
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
    @Component
    private MavenProject project;

    public void execute() {
        logWelcome();
        DeadCode deadCode = analyzeCode();
        log(deadCode);
        logGoodbye();
    }

    private DeadCode analyzeCode() {
        DeadCodeFinder deadCodeFinder = new DeadCodeFinder();
        return deadCodeFinder.findDeadCode(directoriesToAnalyze());
    }

    private File[] directoriesToAnalyze() {
        List<File> files = newArrayList();
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        files.add(outputDirectory);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Going to analyze output directory [" + outputDirectory + "]");
        }
        Plugin plugin = project.getPlugin("org.apache.maven.plugins:maven-war-plugin");
        if (plugin != null) {
            Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
            Xpp3Dom warSourceDirectoryConfig = configuration == null ? null : configuration.getChild("warSourceDirectory");
            if (warSourceDirectoryConfig != null) {
                File warSourceDirectory = new File(warSourceDirectoryConfig.getValue());
                files.add(warSourceDirectory);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Going to analyze custom war source directory [" + warSourceDirectory + "]");
                }
            } else {
                File warSourceDirectory = new File(project.getBasedir(), "src/main/webapp");
                if (warSourceDirectory.exists()) {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Going to analyze default war source directory [" + warSourceDirectory + "]");
                    }
                    files.add(warSourceDirectory);
                } else {
                    getLog().warn("Although maven-war-plugin is used, I cannot find a warSourceDirectory to consider!");
                }
            }
        }
        return files.toArray(new File[files.size()]);
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

    private void logWelcome() {
        if (CLI.equals(mojoExecution.getSource())) {
            getLog().info("Thanks for calling me! Let's see what I can do for you...");
        }
    }

    private void logGoodbye() {
        if (CLI.equals(mojoExecution.getSource())) {
            getLog().info("Expected something different? Don't like the results? " +
                    "Hop on over to https://github.com/ImmobilienScout24/deadcode4j to learn more!");
        }
    }

}
