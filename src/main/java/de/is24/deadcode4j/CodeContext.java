package de.is24.deadcode4j;

import javassist.ClassPool;

import javax.annotation.Nonnull;
import java.io.File;

public class CodeContext {

    private final File[] codeRepositories;
    private final ClassLoader classLoader;
    private final ClassPool classPool;

    public CodeContext(@Nonnull File[] codeRepositories,@Nonnull ClassLoader classLoader,@Nonnull ClassPool classPool) {
        this.codeRepositories = codeRepositories;
        this.classLoader = classLoader;
        this.classPool = classPool;
    }

    @Nonnull
    public File[] getCodeRepositories() {
        return codeRepositories;
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
