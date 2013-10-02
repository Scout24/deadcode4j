package de.is24.deadcode4j;

import javassist.ClassPool;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;

/**
 * The <code>CodeContext</code> provides access to the code repositories and other convenient tools.
 *
 * @since 1.0.2
 */
public class CodeContext {

    private final File[] codeRepositories;
    private final ClassLoader classLoader;
    private final ClassPool classPool;

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

}
