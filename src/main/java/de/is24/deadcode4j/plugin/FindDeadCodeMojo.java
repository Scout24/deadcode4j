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

        int numberOfUnusedClasses = deadCode.getDeadClasses().size();
        if (numberOfUnusedClasses == 0) {
            log.info("No unused classes found. Rejoice!");
            return;
        }

        log.warn("Found " + numberOfUnusedClasses + " unused class(es):");
        for (String unusedClass : Ordering.natural().sortedCopy(deadCode.getDeadClasses())) {
            log.warn("  " + unusedClass);
        }
    }

}
