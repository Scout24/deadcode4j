package de.is24.deadcode4j.plugin;

import com.google.common.collect.Ordering;
import de.is24.deadcode4j.DeadCode;
import org.apache.maven.plugin.logging.Log;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

/**
 * The <code>DeadCodeLogger</code> is responsible for logging the findings of a code analysis.
 *
 * @since 1.3
 */
public class DeadCodeLogger {

    private final Log log;

    public DeadCodeLogger(Log log) {
        this.log = log;
    }

    public void log(@Nonnull DeadCode deadCode, @Nonnull Iterable<String> classesToIgnore) {
        logAnalyzedClasses(deadCode.getAnalyzedClasses());

        Collection<String> deadClasses = newArrayList(deadCode.getDeadClasses());
        removeAndLogIgnoredClasses(deadClasses, classesToIgnore);

        logDeadClasses(deadClasses);
    }

    private void logAnalyzedClasses(@Nonnull Collection<String> analyzedClasses) {
        log.info("Analyzed " + analyzedClasses.size() + " class(es).");
    }

    private void removeAndLogIgnoredClasses(@Nonnull Collection<String> deadClasses, @Nonnull Iterable<String> classesToIgnore) {
        final int numberOfUnusedClasses = deadClasses.size();
        for (String ignoredClass : classesToIgnore) {
            if (!deadClasses.remove(ignoredClass)) {
                log.warn("Class [" + ignoredClass + "] should be ignored, but is not dead. You should remove the configuration entry.");
            }
        }

        int removedClasses = numberOfUnusedClasses - deadClasses.size();
        if (removedClasses != 0) {
            log.info("Ignoring " + removedClasses + " class(es) which seem(s) to be unused.");
        }
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
