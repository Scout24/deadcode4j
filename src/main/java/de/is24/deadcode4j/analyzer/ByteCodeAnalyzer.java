package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.util.Arrays.asList;

/**
 * Serves as a base class with which to analyze byte code (classes).
 *
 * @since 1.3
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public abstract class ByteCodeAnalyzer extends AnalyzerAdapter {

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
    @Nonnull
    protected final ClassPool getClassPool(@Nonnull CodeContext codeContext) {
        return classPoolAccessorFor(codeContext).getClassPool();
    }

    private void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull File clazz) {
        CtClass ctClass = classPoolAccessorFor(codeContext).loadClass(clazz);
        logger.debug("Analyzing class [{}]...", ctClass.getName());
        analyzeClass(codeContext, ctClass);
    }

}
