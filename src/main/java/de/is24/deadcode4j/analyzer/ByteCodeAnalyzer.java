package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.Repository;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
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
     * Returns the class hierarchy for the specified class.
     *
     * @since 1.6
     */
    protected final List<String> getClassHierarchy(CtClass clazz) throws NotFoundException {
        List<String> classes = newArrayList();
        do {
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

    private ClassPool getOrCreateClassPool(CodeContext codeContext) {
        ClassPool classPool = (ClassPool) codeContext.getCache().get(ByteCodeAnalyzer.class);
        if (classPool == null) {
            classPool = createClassPool(codeContext);
            codeContext.getCache().put(ByteCodeAnalyzer.class, classPool);
        }
        return classPool;
    }

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
            throw new RuntimeException("Failed to set up ByteCodeAnalyzer!", e);
        }
        return classPool;
    }

}
