package de.is24.deadcode4j.analyzer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.is24.deadcode4j.CodeContext;
import de.is24.guava.NonNullFunction;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
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
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static de.is24.guava.NonNullFunctions.toFunction;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.util.Arrays.asList;

/**
 * Serves as a base class with which to analyze byte code (classes).
 *
 * @see de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor
 * @since 1.3
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public abstract class ByteCodeAnalyzer extends AnalyzerAdapter {

    private static final NonNullFunction<CodeContext, LoadingCache<File, CtClass>> SUPPLIER =
            new NonNullFunction<CodeContext, LoadingCache<File, CtClass>>() {
                @Nonnull
                @Override
                public LoadingCache<File, CtClass> apply(@Nonnull final CodeContext codeContext) {
                    return CacheBuilder.newBuilder().
                            concurrencyLevel(1).
                            maximumSize(1). // this is fine as long as we process files sequentially
                            build(CacheLoader.from(toFunction(new NonNullFunction<File, CtClass>() {
                        @Nonnull
                        @Override
                        public CtClass apply(@Nonnull File file) {
                            FileInputStream in = null;
                            try {
                                in = new FileInputStream(file);
                                return classPoolAccessorFor(codeContext).getClassPool().makeClass(in);
                            } catch (IOException e) {
                                throw new RuntimeException("Could not load class from [" + file + "]!", e);
                            } finally {
                                IOUtils.closeQuietly(in);
                            }
                        }
                    })));
                }
            };

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

    private void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull File clazz) {
        CtClass ctClass = getOrCreateClassLoader(codeContext).getUnchecked(clazz);
        logger.debug("Analyzing class [{}]...", ctClass.getName());
        analyzeClass(codeContext, ctClass);
    }

    private LoadingCache<File, CtClass> getOrCreateClassLoader(CodeContext codeContext) {
        return codeContext.getOrCreateCacheEntry(ByteCodeAnalyzer.class, SUPPLIER);
    }

}
