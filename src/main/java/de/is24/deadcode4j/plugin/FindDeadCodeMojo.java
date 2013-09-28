package de.is24.deadcode4j.plugin;

import com.google.common.collect.Ordering;
import de.is24.deadcode4j.DeadCode;
import de.is24.deadcode4j.DeadCodeFinder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.maven.plugins.annotations.LifecyclePhase.COMPILE;

/**
 * Finds dead (i.e. unused) code.
 *
 * @since 1.0.0
 */
@Mojo(name = "find", threadSafe = true)
@Execute(phase = COMPILE)
public class FindDeadCodeMojo extends AbstractMojo {

    @Component
    private MavenProject project;
    /**
     * Lists the "dead" classes that should be ignored.
     *
     * @since 1.0.1
     */
    @Parameter
    private Set<String> classesToIgnore;

    public void execute() {
        DeadCode deadCode = analyzeCode();
        log(deadCode);
    }

    private DeadCode analyzeCode() {
        DeadCodeFinder deadCodeFinder = new DeadCodeFinder();
        return deadCodeFinder.findDeadCode(outputDirectoryOfProject());
    }

    private File outputDirectoryOfProject() {
        return new File(project.getBuild().getOutputDirectory());
    }

    private void log(DeadCode deadCode) {
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

}
