package de.is24.deadcode4j.analyzer;

import com.google.common.collect.Lists;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.AnalysisSink;
import de.is24.deadcode4j.AnalyzedCode;
import de.is24.deadcode4j.DeadCodeComputer;
import javassist.CtClass;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
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
        }
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisSink analysisSink, @Nonnull AnalyzedCode analyzedCode) {
        ArrayList<String> ignoredButUnknownClasses = Lists.newArrayList(this.classesToIgnore);
        ignoredButUnknownClasses.removeAll(this.ignoredClasses);
        for (String ignoredButUnknownClass : ignoredButUnknownClasses) {
            logger.warn("Class [{}] should be ignored, but does not exist. You should remove the configuration entry.",
                    ignoredButUnknownClass);
        }

        Collection<String> deadClasses = new DeadCodeComputer().computeDeadCode(analyzedCode).getDeadClasses();
        ArrayList<String> ignoredButExistingClasses = Lists.newArrayList(this.ignoredClasses);
        ignoredButExistingClasses.removeAll(deadClasses);
        for (String ignoredButExistingClass : ignoredButExistingClasses) {
            logger.warn("Class [{}] should be ignored, but is not dead. You should remove the configuration entry.",
                    ignoredButExistingClass);
            this.ignoredClasses.remove(ignoredButExistingClass);
        }

        if (this.ignoredClasses.size() != 0) {
            logger.info("Ignoring {} class(es) which seem(s) to be unused.", this.ignoredClasses.size());
        }
        analysisSink.addDependencies("_IgnoredClasses_",
                this.ignoredClasses.toArray(new String[this.ignoredClasses.size()]));
    }

}
