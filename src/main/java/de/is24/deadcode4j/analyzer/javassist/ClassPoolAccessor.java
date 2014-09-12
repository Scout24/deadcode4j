package de.is24.deadcode4j.analyzer.javassist;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.Repository;
import de.is24.guava.NonNullFunction;
import de.is24.guava.SequentialLoadingCache;
import javassist.ClassPool;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Sets.newHashSet;

/**
 * The <code>ClassPoolAccessor</code> provides access to a Javassist {@link javassist.ClassPool} with fully configured
 * class path. It also provides some convenience methods to deal with loading & resolving classes.
 *
 * @since 2.0.0
 */
public final class ClassPoolAccessor {
    @Nonnull
    private static final NonNullFunction<AnalysisContext, ClassPoolAccessor> SUPPLIER = new NonNullFunction<AnalysisContext, ClassPoolAccessor>() {
        @Nonnull
        @Override
        public ClassPoolAccessor apply(@Nonnull AnalysisContext input) {
            return new ClassPoolAccessor(input);
        }
    };
    @Nonnull
    private final ClassPool classPool;
    @Nonnull
    private final LoadingCache<String, Optional<String>> classResolver;

    public ClassPoolAccessor(@Nonnull AnalysisContext analysisContext) {
        this.classPool = createClassPool(analysisContext);
        this.classResolver = createResolverCache();
    }

    /**
     * Creates or retrieves the <code>ClassPoolAccessor</code> for the given analysis context.<br/>
     * A new instance will be put in the analysis context's cache and subsequently retrieved from there.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static ClassPoolAccessor classPoolAccessorFor(@Nonnull AnalysisContext analysisContext) {
        return analysisContext.getOrCreateCacheEntry(ClassPoolAccessor.class, SUPPLIER);
    }

    @Nonnull
    private static ClassPool createClassPool(AnalysisContext analysisContext) {
        ClassPool classPool = new ClassPool(true);
        try {
            Repository outputRepository = analysisContext.getModule().getOutputRepository();
            if (outputRepository != null) {
                classPool.appendClassPath(outputRepository.getDirectory().getAbsolutePath());
            }
            for (File file : analysisContext.getModule().getClassPath()) {
                classPool.appendClassPath(file.getAbsolutePath());
            }
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to set up ClassPool!", e);
        }
        return classPool;
    }

    private static String prepareQualifier(CharSequence qualifier) {
        String preparedQualifier = qualifier.toString();
        for (; ; ) {
            int lastDot = preparedQualifier.lastIndexOf('.');
            int lastDollar = preparedQualifier.lastIndexOf('$');
            if (lastDollar < 0 || lastDot < 0 || lastDollar > lastDot) {
                break;
            }
            preparedQualifier = preparedQualifier.substring(0, lastDot) + "$" + preparedQualifier.substring(lastDot + 1);
        }
        return preparedQualifier;
    }

    /**
     * Returns the <code>ClassPool</code> used for examining classes.
     *
     * @since 2.0.0
     */
    @Nonnull
    public ClassPool getClassPool() {
        return this.classPool;
    }

    /**
     * Returns the "resolved" class name for the given qualifier.
     * "Resolved" in this case means that if the qualifier refers to an existing class, the class'
     * {@link java.lang.ClassLoader binary name} is returned.
     *
     * @since 2.0.0
     */
    @Nonnull
    public Optional<String> resolveClass(@Nonnull CharSequence qualifier) {
        return classResolver.getUnchecked(prepareQualifier(qualifier));
    }

    @Nonnull
    private LoadingCache<String, Optional<String>> createResolverCache() {
        return new SequentialLoadingCache<String, String>(new Function<String, Optional<String>>() {
            @Nonnull
            private final Set<String> knownPackages = newHashSet();

            @Nullable
            @Override
            public Optional<String> apply(@Nullable String input) {
                if (input == null) {
                    return absent();
                }
                for (; ; ) {
                    if (classPool.getOrNull(input) != null) {
                        addToKnownPackages(input);
                        return of(input);
                    }
                    int dotIndex = input.lastIndexOf('.');
                    if (dotIndex < 0) {
                        return absent();
                    }
                    String potentialPackage = input.substring(0, dotIndex);
                    if (knownPackages.contains(potentialPackage)) {
                        // no need to look for inner classes
                        return absent();
                    }
                    input = potentialPackage + "$" + input.substring(dotIndex + 1);
                }
            }

            private void addToKnownPackages(@Nonnull String className) {
                for (; ; ) {
                    int dotIndex = className.lastIndexOf('.');
                    if (dotIndex < 0) {
                        return;
                    }
                    className = className.substring(0, dotIndex);
                    if (!knownPackages.add(className)) {
                        return;
                    }
                }

            }

        });
    }

}
