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
     * @since 1.6
     */
    @Nonnull
    public static Set<String> getAllImplementedInterfaces(@Nonnull final CtClass clazz) throws NotFoundException {
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

}
