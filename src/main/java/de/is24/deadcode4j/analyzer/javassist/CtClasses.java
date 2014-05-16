package de.is24.deadcode4j.analyzer.javassist;

import javassist.CtClass;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Provides convenience methods with which to analyze instances of {@link javassist.CtClass}.
 *
 * @since 1.6
 */
public final class CtClasses {

    private CtClasses() {
    }

    /**
     * Retrieves all interfaces a class implements - either directly, via superclass or via interface inheritance.
     *
     * @throws java.lang.RuntimeException if an implemented interface or super class cannot be loaded
     * @since 1.6
     */
    @Nonnull
    public static Set<String> getAllImplementedInterfaces(@Nonnull final CtClass clazz) {
        Set<String> interfaces = newHashSet();
        CtClass loopClass = clazz;
        try {
            for (; ; ) {
                for (CtClass anInterface : loopClass.getInterfaces()) {
                    interfaces.add(anInterface.getName());
                    interfaces.addAll(getAllImplementedInterfaces(anInterface));
                }
                loopClass = loopClass.getSuperclass();
                if (loopClass == null || "java.lang.Object".equals(loopClass.getName())) {
                    return interfaces;
                }
            }
        } catch (NotFoundException e) {
            throw new RuntimeException("The class path is not correctly set up; could not load " + e.getMessage() + "!");
        }
    }

}
