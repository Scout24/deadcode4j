package de.is24.deadcode4j.plugin;

import com.google.common.collect.Ordering;
import de.is24.deadcode4j.AnalysisStage;
import de.is24.deadcode4j.DeadCode;
import org.apache.maven.plugin.logging.Log;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;

/**
 * The <code>DeadCodeLogger</code> is responsible for logging the findings of a code analysis.
 *
 * @since 1.3
 */
class DeadCodeLogger {

    private final Log log;

    public DeadCodeLogger(Log log) {
        this.log = log;
    }

    public void log(@Nonnull DeadCode deadCode) {
        logExceptions(deadCode.getStagesWithExceptions());
        logAnalyzedClasses(deadCode.getAnalyzedClasses());
        logDeadClasses(deadCode.getDeadClasses());
    }

    private void logExceptions(EnumSet<AnalysisStage> stagesWithExceptions) {
        for (AnalysisStage stageWithException : stagesWithExceptions) {
            switch (stageWithException) {
                case GENERAL_SETUP:
                    log.error("Failed to set up deadcode4j. Analysis was certainly inaccurate.");
                    break;
                case MODULE_SETUP:
                    log.warn("Failed to set up at least one module. Analysis is likely to be inaccurate.");
                    break;
                case FILE_ANALYSIS:
                    log.warn("At least one file could not be parsed; analysis may be inaccurate!");
                    break;
                case DEADCODE_ANALYSIS:
                    log.warn("Failed to determine dead code. Analysis was certainly inaccurate.");
                    break;
                default:
                    log.warn("An unexpected error occurred.");
                    break;
            }
        }
    }

    private void logAnalyzedClasses(@Nonnull Collection<String> analyzedClasses) {
        log.info("Analyzed " + analyzedClasses.size() + " class(es).");
    }

    private void logDeadClasses(@Nonnull Collection<String> deadClasses) {
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
