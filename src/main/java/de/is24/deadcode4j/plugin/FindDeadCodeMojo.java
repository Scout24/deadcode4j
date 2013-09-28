package de.is24.deadcode4j.plugin;

import com.google.common.collect.Ordering;
import de.is24.deadcode4j.DeadCode;
import de.is24.deadcode4j.DeadCodeFinder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * The FindDeadCodeMojo attempts to find unused code.
 *
 * @description Finds dead (i.e. unused) code
 * @goal find
 * @execute phase="compile"
 * @threadSafe true
 */
@SuppressWarnings("UnusedDeclaration")
public class FindDeadCodeMojo extends AbstractMojo {

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * Lists the "dead" classes that should be ignored.
     *
     * @parameter
     */
    private Set<String> classesToIgnore;

    public void execute() throws MojoExecutionException, MojoFailureException {
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
        Log log = getLog();

        log.info("Analyzed " + deadCode.getAnalyzedClasses().size() + " class(es).");

        Collection<String> deadClasses = newArrayList(deadCode.getDeadClasses());
        int numberOfUnusedClasses = deadClasses.size();
        if (this.classesToIgnore != null) {
            for (String ignoredClass : this.classesToIgnore) {
                if (!deadClasses.remove(ignoredClass)) {
                    log.warn("Class [" + ignoredClass + "] should be ignored, but is not dead. You should remove the configuration entry.");
                }
            }
            int removedClasses = numberOfUnusedClasses - deadClasses.size();
            if (removedClasses != 0) {
                log.info("Ignoring " + removedClasses + " class(es) which seem(s) to be unused.");
            }
            numberOfUnusedClasses = deadClasses.size();
        }
        if (numberOfUnusedClasses == 0) {
            log.info("No unused classes found. Rejoice!");
            return;
        }

        log.warn("Found " + numberOfUnusedClasses + " unused class(es):");
        for (String unusedClass : Ordering.natural().sortedCopy(deadClasses)) {
            log.warn("  " + unusedClass);
        }
    }

}
