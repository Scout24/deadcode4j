package de.is24.deadcode4j.analyzer.javassist;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.Repository;
import javassist.ClassPool;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Sets.newHashSet;

public final class ClassPoolAccessor {
    @Nonnull
    private final ClassPool classPool;
    @Nonnull
    private final LoadingCache<String, Optional<String>> classResolver;
    @Nonnull
    private final Set<String> knownPackages = newHashSet();

    public ClassPoolAccessor(@Nonnull CodeContext codeContext) {
        this.classPool = createClassPool(codeContext);
        this.classResolver = createResolverCache();
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
    public ClassPool getClassPool() {
        return this.classPool;
    }

    /**
     * Returns the "resolved" class name for the given qualifier.
     * "Resolved" in this case means that if the qualifier refers to an existing class, the class'
     * {@link java.lang.ClassLoader binary name} is returned.
     *
     * @since 1.6
     */
    @Nonnull
    public Optional<String> resolveClass(@Nonnull CharSequence qualifier) {
        return classResolver.getUnchecked(qualifier.toString());
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

    private LoadingCache<String, Optional<String>> createResolverCache() {
        return CacheBuilder.newBuilder().concurrencyLevel(1).build(CacheLoader.from(new Function<String, Optional<String>>() {
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
        }));
    }

    private void addToKnownPackages(String className) {
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

}
