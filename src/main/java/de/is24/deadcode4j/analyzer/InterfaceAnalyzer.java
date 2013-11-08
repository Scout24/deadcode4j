package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import javassist.CtClass;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.disjoint;

/**
 * Serves as a base class with which to mark classes as being in use if they implement one of the specified interfaces.
 *
 * @since 1.4
 */
public abstract class InterfaceAnalyzer extends ByteCodeAnalyzer implements Analyzer {

    private final String dependerId;
    private final Set<String> interfaceClasses;

    /**
     * Creates a new <code>InterfaceAnalyzer</code>.
     *
     * @param dependerId     a description of the <i>depending entity</i> with which to
     *                       call {@link de.is24.deadcode4j.CodeContext#addDependencies(String, java.util.Collection)}
     * @param interfaceNames a list of fully qualified interface names indicating that the implementing class is still
     *                       in use
     * @since 1.4
     */
    protected InterfaceAnalyzer(@Nonnull String dependerId, @Nonnull String... interfaceNames) {
        this.dependerId = dependerId;
        this.interfaceClasses = newHashSet(interfaceNames);
        if (this.interfaceClasses.isEmpty()) {
            throw new IllegalArgumentException("interfaceNames cannot by empty!");
        }
    }

    @Override
    protected void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        String clazzName = clazz.getName();
        codeContext.addAnalyzedClass(clazzName);

        if (!disjoint(this.interfaceClasses, asList(clazz.getClassFile2().getInterfaces()))) {
            codeContext.addDependencies(this.dependerId, clazzName);
        }
    }

}
