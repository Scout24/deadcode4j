package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.util.Set;

import static de.is24.deadcode4j.analyzer.javassist.CtClasses.getAllImplementedInterfaces;

public class SpringDataCustomRepositoriesAnalyzer extends ByteCodeAnalyzer {

    @Override
    protected void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        final String clazzName = clazz.getName();
        codeContext.addAnalyzedClass(clazzName);
        int modifiers = clazz.getModifiers();
        if (!Modifier.isInterface(modifiers)) {
            return;
        }
        Set<String> implementedInterfaces;
        try {
            implementedInterfaces = getAllImplementedInterfaces(clazz);
        } catch (NotFoundException e) {
            logger.warn("The class path is not correctly set up; could not load [{}]! Skipping interfaces check for {}.", e.getMessage(), clazzName);
            return;
        }
        if (!implementedInterfaces.contains("org.springframework.data.repository.Repository")) {
            return;
        }
        final String nameOfCustomRepositoryInterface = clazzName + "Custom";
        if (!implementedInterfaces.contains(nameOfCustomRepositoryInterface)) {
            return;
        }
        // TODO we should store the custom interface as there may be implementations in other modules?!?
        final String nameOfCustomRepositoryImplementation = clazzName + "Impl";
        CtClass customImpl = ClassPoolAccessor.classPoolAccessorFor(codeContext).getClassPool().getOrNull(nameOfCustomRepositoryImplementation);
        if (customImpl == null) {
            return;
        }
        try {
            implementedInterfaces = getAllImplementedInterfaces(customImpl);
        } catch (NotFoundException e) {
            logger.warn("The class path is not correctly set up; could not load [{}]! Skipping interfaces check for {}.", e.getMessage(), customImpl.getName());
            return;
        }
        if (implementedInterfaces.contains(nameOfCustomRepositoryInterface)) {
            codeContext.addDependencies(clazzName, nameOfCustomRepositoryImplementation);
        }
    }

}
