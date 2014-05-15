package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static de.is24.deadcode4j.IntermediateResults.*;
import static de.is24.deadcode4j.analyzer.javassist.CtClasses.getAllImplementedInterfaces;

public class SpringDataCustomRepositoriesAnalyzer extends ByteCodeAnalyzer {

    private List<String> customRepositoryNames = newArrayList();

    @Override
    protected void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        final String clazzName = clazz.getName();
        codeContext.addAnalyzedClass(clazzName);
        int modifiers = clazz.getModifiers();
        if (Modifier.isInterface(modifiers)) {
            Set<String> implementedInterfaces;
            try {
                implementedInterfaces = getAllImplementedInterfaces(clazz);
            } catch (NotFoundException e) {
                logger.warn("The class path is not correctly set up; could not load [{}]! Skipping Spring Data custom repository check for {}.", e.getMessage(), clazzName);
                return;
            }
            if (!implementedInterfaces.contains("org.springframework.data.repository.Repository")) {
                return;
            }
            final String nameOfCustomRepositoryInterface = clazzName + "Custom";
            if (!implementedInterfaces.contains(nameOfCustomRepositoryInterface)) {
                return;
            }
            this.customRepositoryNames.add(nameOfCustomRepositoryInterface);
            final String nameOfCustomRepositoryImplementation = clazzName + "Impl";
            CtClass customImpl = ClassPoolAccessor.classPoolAccessorFor(codeContext).getClassPool().getOrNull(nameOfCustomRepositoryImplementation);
            if (customImpl == null) {
                return;
            }
            try {
                implementedInterfaces = getAllImplementedInterfaces(customImpl);
            } catch (NotFoundException e) {
                logger.warn("The class path is not correctly set up; could not load [{}]! Skipping Spring Data custom repository check for {}.", e.getMessage(), clazz.getName());
                return;
            }
            if (implementedInterfaces.contains(nameOfCustomRepositoryInterface)) {
                codeContext.addDependencies(clazzName, nameOfCustomRepositoryImplementation);
            }
        } else if (Modifier.isAbstract(modifiers)
                || Modifier.isAnnotation(modifiers)
                || Modifier.isEnum(modifiers)
                || Modifier.isPrivate(modifiers)
                || Modifier.isProtected(modifiers)) {
            return;
        }
        IntermediateResultSet<String> intermediateResults = resultSetFrom(codeContext, getClass());
        if (intermediateResults == null) {
            return;
        }
        Set<String> inheritedCustomRepositories = intermediateResults.getResults();
        Set<String> implementedInterfaces;
        try {
            implementedInterfaces = getAllImplementedInterfaces(clazz);
        } catch (NotFoundException e) {
            logger.warn("The class path is not correctly set up; could not load [{}]! Skipping Spring Data custom repository check for {}.", e.getMessage(), clazzName);
            return;
        }
        implementedInterfaces.retainAll(inheritedCustomRepositories);
        for (String customRepositoryName : implementedInterfaces) {
            codeContext.addDependencies(customRepositoryName.substring(0, customRepositoryName.length() - "Custom".length()), clazzName);
        }
    }

    @Override
    public void finishAnalysis(@Nonnull CodeContext codeContext) {
        codeContext.getCache().put(getClass(), resultSetFor(this.customRepositoryNames));
        this.customRepositoryNames.clear();
    }

}
