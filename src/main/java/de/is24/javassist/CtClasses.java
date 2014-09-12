package de.is24.javassist;

import com.google.common.collect.Lists;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

/**
 * Provides convenience methods with which to analyze instances of {@link javassist.CtClass}.
 *
 * @since 2.0.0
 */
public final class CtClasses {

    private CtClasses() {}

    private static boolean issuedWarningForJavaLangAnnotationRepeatable = false;

    /**
     * Retrieves the specified class.
     * This method swallows class loading issues, returning only those classes that are accessible by the
     * {@link javassist.CtClass#getClassPool() class pool}.
     *
     * @since 2.0.0
     */
    @Nullable
    public static CtClass getCtClass(@Nonnull ClassPool classPool, @Nonnull String className) {
        CtClass clazz = classPool.getOrNull(className);
        if (clazz == null) {
            handleMissingClass(className);
        }
        return clazz;
    }

    /**
     * Returns {@code true} if the specified class refers to {@link java.lang.Object}.
     *
     * @since 2.0.0
     */
    public static boolean isJavaLangObject(@Nullable CtClass loopClass) {
        return loopClass != null && "java.lang.Object".equals(loopClass.getName());
    }

    /**
     * Retrieves all interfaces a class implements - either directly, via superclass or via interface inheritance.
     *
     * @throws java.lang.RuntimeException if an implemented interface or super class cannot be loaded
     * @see #getInterfacesOf(javassist.CtClass)
     * @since 2.0.0
     */
    @Nonnull
    public static Set<String> getAllImplementedInterfaces(@Nonnull final CtClass clazz) {
        Set<String> interfaces = newHashSet();
        CtClass loopClass = clazz;
        do {
            for (CtClass anInterface : getInterfacesOf(loopClass)) {
                interfaces.add(anInterface.getName());
                interfaces.addAll(getAllImplementedInterfaces(anInterface));
            }
            loopClass = getSuperclassOf(loopClass);
        } while (loopClass != null && !isJavaLangObject(loopClass));
        return interfaces;
    }

    /**
     * Retrieves all interfaces a class directly implements.
     * This method swallows class loading issues, returning only those classes that are accessible by the
     * {@link javassist.CtClass#getClassPool() class pool}.
     *
     * @see #getAllImplementedInterfaces(javassist.CtClass)
     * @since 2.0.0
     */
    @Nonnull
    public static Iterable<CtClass> getInterfacesOf(@Nonnull CtClass clazz) {
        String[] interfaceNames = clazz.getClassFile2().getInterfaces();
        Collection<CtClass> interfaces = Lists.newArrayListWithCapacity(interfaceNames.length);
        for (String nameOfInterface : interfaceNames) {
            CtClass interfaze = getCtClass(clazz, nameOfInterface);
            if (interfaze != null) {
                interfaces.add(interfaze);
            }
        }
        return interfaces;
    }

    /**
     * Retrieves the superclass.
     * This method swallows class loading issues, returning only those classes that are accessible by the
     * {@link javassist.CtClass#getClassPool() class pool}.
     *
     * @since 2.0.0
     */
    @Nullable
    public static CtClass getSuperclassOf(@Nonnull CtClass clazz) {
        String nameOfSuperclass = clazz.getClassFile2().getSuperclass();
        return getCtClass(clazz, nameOfSuperclass);
    }


    /**
     * Retrieves the declaring classes in bottom-up order.
     * This method swallows class loading issues, returning only those classes that are accessible by the
     * {@link javassist.CtClass#getClassPool() class pool}.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static Iterable<CtClass> getDeclaringClassesOf(@Nonnull CtClass clazz) {
        Collection<CtClass> declaringClasses = Lists.newArrayList();
        for (CtClass declarer = clazz; declarer != null; declarer = getDeclaringClassOf(declarer)) {
            declaringClasses.add(declarer);
        }
        return declaringClasses;
    }

    /**
     * Retrieves the nested classes.
     * This method swallows class loading issues, returning only those classes that are accessible by the
     * {@link javassist.CtClass#getClassPool() class pool}.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static Iterable<CtClass> getNestedClassesOf(@Nonnull CtClass clazz) {
        try {
            return asList(clazz.getNestedClasses());
        } catch (NotFoundException e) {
            handleMissingClass(e.getMessage());
            return Collections.emptyList();
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(CtClasses.class);
    }

    private static void handleMissingClass(@Nonnull String className) {
        if ("java.lang.annotation.Repeatable".equals(className)) {
            if (issuedWarningForJavaLangAnnotationRepeatable) {
                return;
            }
            getLogger().warn("Running with JDK < 8, but classes or libraries refer to JDK8 annotation {}.", className);
            issuedWarningForJavaLangAnnotationRepeatable = true;
        } else {
            getLogger().warn("The class path is not correctly set up; could not load {}!", className);
        }
    }

    @Nullable
    private static CtClass getCtClass(@Nonnull CtClass classProvidingPool, @Nonnull String className) {
        return getCtClass(classProvidingPool.getClassPool(), className);
    }

    @Nullable
    private static CtClass getDeclaringClassOf(@Nonnull CtClass clazz) {
        try {
            return clazz.getDeclaringClass();
        } catch (NotFoundException e) {
            handleMissingClass(e.getMessage());
            return null;
        }
    }

}
