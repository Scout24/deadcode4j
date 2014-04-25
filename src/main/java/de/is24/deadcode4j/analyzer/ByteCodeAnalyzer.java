package de.is24.deadcode4j.analyzer;

import com.google.common.base.Function;
import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.Repository;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.util.Arrays.asList;

/**
 * Serves as a base class with which to analyze byte code (classes).
 *
 * @since 1.3
 */
public abstract class ByteCodeAnalyzer extends AnalyzerAdapter implements Analyzer {

    /**
     * Retrieves all annotations of a package/class and its members (if requested).
     *
     * @param clazz        the <code>CtClass</code> to examine
     * @param elementTypes indicates which annotations to retrieve
     * @since 1.4
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    protected static Iterable<Annotation> getAnnotations(@Nonnull CtClass clazz, ElementType... elementTypes) {
        List<ElementType> types = asList(elementTypes);
        List<AttributeInfo> attributes = newArrayList();

        if (clazz.getName().endsWith("package-info") && types.contains(ElementType.PACKAGE) ||
                !clazz.getName().endsWith("package-info") && types.contains(ElementType.TYPE)) {
            attributes.addAll(clazz.getClassFile2().getAttributes());
        }
        if (types.contains(METHOD)) {
            for (CtMethod method : clazz.getDeclaredMethods()) {
                attributes.addAll(method.getMethodInfo2().getAttributes());
            }
        }
        if (types.contains(FIELD)) {
            for (CtField field : clazz.getDeclaredFields()) {
                attributes.addAll(field.getFieldInfo2().getAttributes());
            }
        }

        List<Annotation> annotations = newArrayList();
        for (AttributeInfo attribute : attributes) {
            if (AnnotationsAttribute.class.isInstance(attribute)) {
                Collections.addAll(annotations, AnnotationsAttribute.class.cast(attribute).getAnnotations());
            }
        }

        return annotations;
    }

    @Override
    public final void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull File file) {
        if (file.getName().endsWith(".class")) {
            analyzeClass(codeContext, file);
        }
    }

    /**
     * Perform an analysis for the specified class.
     * Results must be reported via the capabilities of the {@link CodeContext}.
     *
     * @since 1.3
     */
    protected abstract void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz);

    /**
     * Returns the <code>ClassPool</code> used for examining classes.
     *
     * @since 1.6
     */
    protected final ClassPool getOrCreateClassPool(@Nonnull CodeContext codeContext) {
        ClassPool classPool = (ClassPool) codeContext.getCache().get(ByteCodeAnalyzer.class);
        if (classPool == null) {
            classPool = createClassPool(codeContext);
            codeContext.getCache().put(ByteCodeAnalyzer.class, classPool);
        }
        return classPool;
    }

    /**
     * Returns the class hierarchy for the specified class.
     *
     * @see #getClassHierarchy(javassist.CtClass, com.google.common.base.Function)
     * @since 1.6
     */
    @Nonnull
    protected final List<String> getClassHierarchy(@Nonnull CtClass clazz) throws NotFoundException {
        return getClassHierarchy(clazz, new Function<CtClass, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable CtClass ctClass) {
                return null;
            }
        });
    }


    /**
     * Returns the class hierarchy for the specified class, calling the specified function for each of the hierarchy's
     * class.
     *
     * @param clazz        the <code>CtClass</code> to examine
     * @param clazzHandler the <code>Function</code> to call for each class
     * @return a <code>List</code> with all class names of the class hierarchy
     * @since 1.6
     */
    @Nonnull
    protected final List<String> getClassHierarchy(@Nonnull CtClass clazz, @Nonnull Function<CtClass, Void> clazzHandler) throws NotFoundException {
        List<String> classes = newArrayList();
        do {
            clazzHandler.apply(clazz);
            classes.add(clazz.getClassFile2().getSuperclass());
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return classes;
    }

    private void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull File clazz) {
        final CtClass ctClass;
        FileInputStream in = null;
        try {
            in = new FileInputStream(clazz);
            ctClass = getOrCreateClassPool(codeContext).makeClass(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not analyze [" + clazz + "]!", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        logger.debug("Analyzing class [{}]...", ctClass.getName());
        analyzeClass(codeContext, ctClass);
    }

    @Nonnull
    private ClassPool createClassPool(@Nonnull CodeContext codeContext) {
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
            throw new RuntimeException("Failed to set up ByteCodeAnalyzer!", e);
        }
        return classPool;
    }

}
