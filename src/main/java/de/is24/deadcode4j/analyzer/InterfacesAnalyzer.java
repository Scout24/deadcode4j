package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import de.is24.guava.NonNullFunction;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static java.util.Collections.disjoint;

/**
 * Serves as a base class with which to mark classes as being in use if they explicitly implement one of the specified
 * interfaces.
 *
 * @since 1.4
 */
public abstract class InterfacesAnalyzer extends ByteCodeAnalyzer {

    @Nonnull
    private final String dependerId;
    @Nonnull
    private final Set<String> interfaceClasses;
    private final NonNullFunction<CodeContext, Set<String>> supplyInterfacesFoundInClassPath = new NonNullFunction<CodeContext, Set<String>>() {
        @Nonnull
        @Override
        public Set<String> apply(@Nonnull CodeContext input) {
            ClassPool classPool = classPoolAccessorFor(input).getClassPool();
            Set<String> knownClasses = newHashSet();
            for (String className : interfaceClasses) {
                CtClass ctClass = classPool.getOrNull(className);
                if (ctClass != null) {
                    knownClasses.add(className);
                }
            }
            logger.debug("Found those interfaces in the class path: {}", knownClasses);
            return knownClasses;
        }
    };

    private InterfacesAnalyzer(@Nonnull String dependerId, @Nonnull Set<String> interfaceNames) {
        this.dependerId = dependerId;
        this.interfaceClasses = interfaceNames;
        checkArgument(!this.interfaceClasses.isEmpty(), "interfaceNames cannot by empty!");
    }

    /**
     * Creates a new <code>InterfacesAnalyzer</code>.
     *
     * @param dependerId     a description of the <i>depending entity</i> with which to
     *                       call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, Iterable)}
     * @param interfaceNames a list of fully qualified interface names indicating that the implementing class is still
     *                       in use
     * @since 1.4
     */
    protected InterfacesAnalyzer(@Nonnull String dependerId, @Nonnull String... interfaceNames) {
        this(dependerId, newHashSet(interfaceNames));
    }

    /**
     * Creates a new <code>InterfacesAnalyzer</code>.
     *
     * @param dependerId     a description of the <i>depending entity</i> with which to
     *                       call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, Iterable)}
     * @param interfaceNames a list of fully qualified interface names indicating that the implementing class is still
     *                       in use
     * @since 1.4
     */
    protected InterfacesAnalyzer(@Nonnull String dependerId, @Nonnull Iterable<String> interfaceNames) {
        this(dependerId, newHashSet(interfaceNames));
    }

    @Override
    protected final void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        Set<String> knownInterfaces = getInterfacesFoundInClassPath(codeContext);
        if (knownInterfaces.isEmpty()) {
            return;
        }
        String clazzName = clazz.getName();
        codeContext.addAnalyzedClass(clazzName);

        final Set<String> allImplementedInterfaces;
        try {
            allImplementedInterfaces = getAllImplementedInterfaces(clazz);
        } catch (NotFoundException e) {
            logger.warn("The class path is not correctly set up; could not load [{}]! Skipping interfaces check for {}.", e.getMessage(), clazz.getName());
            return;
        }
        if (!disjoint(knownInterfaces, allImplementedInterfaces)) {
            codeContext.addDependencies(this.dependerId, clazzName);
        }
    }

    @Nonnull
    private Set<String> getAllImplementedInterfaces(@Nonnull final CtClass clazz) throws NotFoundException {
        Set<String> interfaces = newHashSet();
        CtClass loopClass = clazz;
        do {
            for (CtClass anInterface : loopClass.getInterfaces()) {
                interfaces.add(anInterface.getName());
                interfaces.addAll(getAllImplementedInterfaces(anInterface));
            }
            loopClass = loopClass.getSuperclass();
        } while (loopClass != null && !"java.lang.Object".equals(loopClass.getName()));
        return interfaces;
    }

    @Nonnull
    private Set<String> getInterfacesFoundInClassPath(@Nonnull CodeContext codeContext) {
        return codeContext.getOrCreateCacheEntry(getClass(), supplyInterfacesFoundInClassPath);
    }

}
