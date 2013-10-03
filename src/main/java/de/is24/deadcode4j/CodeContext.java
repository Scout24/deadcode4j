package de.is24.deadcode4j;

import javassist.ClassPool;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * The <code>CodeContext</code> provides access to the code repositories and other convenient tools.
 *
 * @since 1.0.2
 */
public class CodeContext {

    private final File[] codeRepositories;
    private final ClassLoader classLoader;
    private final ClassPool classPool;

    private final Set<String> analyzedClasses = newHashSet();
    private final Map<String, Set<String>> dependencyMap = newHashMap();

    public CodeContext(@Nonnull File[] codeRepositories, @Nonnull ClassLoader classLoader, @Nonnull ClassPool classPool) {
        this.codeRepositories = Arrays.copyOf(codeRepositories, codeRepositories.length);
        this.classLoader = classLoader;
        this.classPool = classPool;
    }

    @Nonnull
    public File[] getCodeRepositories() {
        return Arrays.copyOf(this.codeRepositories, this.codeRepositories.length);
    }

    @Nonnull
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Nonnull
    public ClassPool getClassPool() {
        return classPool;
    }

    public void addDependencies(@Nonnull String depender, @Nonnull Collection<String> dependees) {
        Set<String> existingDependees = dependencyMap.get(depender);
        if (existingDependees == null) {
            existingDependees = new HashSet<String>();
            dependencyMap.put(depender, existingDependees);
        }
        existingDependees.addAll(dependees);
    }

    public void addAnalyzedClass(@Nonnull String clazz) {
        this.analyzedClasses.add(clazz);
    }

    public AnalyzedCode getAnalyzedCode() {
        return new AnalyzedCode(this.analyzedClasses, this.dependencyMap);
    }
}
