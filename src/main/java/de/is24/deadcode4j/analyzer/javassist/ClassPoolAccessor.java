package de.is24.deadcode4j.analyzer.javassist;

import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.Repository;
import javassist.ClassPool;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.io.File;

public final class ClassPoolAccessor {
    @Nonnull
    private final ClassPool classPool;

    public ClassPoolAccessor(@Nonnull CodeContext codeContext) {
        this.classPool = createClassPool(codeContext);
    }

    @Nonnull
    public static ClassPoolAccessor classPoolAccessorFor(@Nonnull CodeContext codeContext) {
        ClassPoolAccessor classPoolAccessor = (ClassPoolAccessor) codeContext.getCache().get(ClassPoolAccessor.class);
        if (classPoolAccessor == null) {
            classPoolAccessor = new ClassPoolAccessor(codeContext);
            codeContext.getCache().put(ClassPoolAccessor.class, classPoolAccessor);
        }
        return classPoolAccessor;
    }

    /**
     * Returns the <code>ClassPool</code> used for examining classes.
     *
     * @since 1.6
     */
    @Nonnull
    public final ClassPool getClassPool() {
        return this.classPool;
    }

    @Nonnull
    private ClassPool createClassPool(CodeContext codeContext) {
        ClassPool classPool = new ClassPool(true);
        try {
            Repository outputRepository = codeContext.getModule().getOutputRepository();
            if (outputRepository != null) {
                classPool.appendClassPath(outputRepository.getDirectory().getAbsolutePath());
            }
            for (File file : codeContext.getModule().getClassPath()) {
                classPool.appendClassPath(file.getAbsolutePath());
            }
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to set up ClassPool!", e);
        }
        return classPool;
    }

}
