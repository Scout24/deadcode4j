package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * The <code>AnalyzedCode</code> provides access to the code repositories and other convenient tools.
 *
 * @since 1.0.0
 */
public class AnalyzedCode {
    private final Collection<String> analyzedClasses;
    private final Map<String, Iterable<String>> dependenciesForClass;

    public AnalyzedCode(@Nonnull Collection<String> analyzedClasses, @Nonnull Map<String, Iterable<String>> dependenciesForClass) {
        this.analyzedClasses = analyzedClasses;
        this.dependenciesForClass = dependenciesForClass;
    }

    @Nonnull
    public Collection<String> getAnalyzedClasses() {
        return analyzedClasses;
    }

    @Nonnull
    public Map<String, Iterable<String>> getDependenciesForClass() {
        return dependenciesForClass;
    }

    public AnalyzedCode merge(AnalyzedCode analyzedCode) {
        ArrayList<String> combinedAnalyzedClasses = newArrayList(this.analyzedClasses);
        combinedAnalyzedClasses.addAll(analyzedCode.getAnalyzedClasses());
        HashMap<String, Iterable<String>> combinedDependenciesForClass = newHashMap(this.dependenciesForClass);
        combinedDependenciesForClass.putAll(analyzedCode.getDependenciesForClass());
        return new AnalyzedCode(combinedAnalyzedClasses, combinedDependenciesForClass);
    }

}
