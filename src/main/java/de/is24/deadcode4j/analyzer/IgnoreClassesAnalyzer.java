package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.DeadCode;
import javassist.CtClass;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;

public class IgnoreClassesAnalyzer extends ByteCodeAnalyzer {

    @Nonnull
    private final Set<String> classesToIgnore;
    @Nonnull
    private final Set<String> ignoredClasses;

    public IgnoreClassesAnalyzer(@Nonnull Set<String> classesToIgnore) {
        this.classesToIgnore = newHashSet(classesToIgnore);
        this.ignoredClasses = newHashSetWithExpectedSize(classesToIgnore.size());
    }

    @Override
    protected void analyzeClass(@Nonnull AnalysisContext analysisContext, @Nonnull CtClass clazz) {
        String className = clazz.getName();
        analysisContext.addAnalyzedClass(className);
        if (classesToIgnore.contains(className)) {
            this.ignoredClasses.add(className);
            analysisContext.addDependencies("_IgnoredClasses_", className);
        }
    }

    @Override
    public void finishAnalysis(@Nonnull DeadCode deadCode) {
        if (this.ignoredClasses.size() != 0) {
            logger.info("Ignoring {} class(es) which seem(s) to be unused.");
        }
        for (String ignoredClass : ignoredClasses) {
            if (!deadCode.getDeadClasses().contains(ignoredClass)) {
                logger.warn("Class [{}] should be ignored, but is not dead. You should remove the configuration entry.",
                        ignoredClass);
            }
        }
    }

}
