package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import javassist.CtClass;
import javassist.Modifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static de.is24.deadcode4j.IntermediateResults.*;
import static de.is24.javassist.CtClasses.getAllImplementedInterfaces;

/**
 * Analyzes class files: marks custom implementations of Spring Data repositories as being in use.<br/>
 * Note that this analyzer only considers the default naming convention <code><i>RepositoryName</i>Impl</code> to
 * identify custom implementations.
 *
 * @since 2.0.0
 */
public class SpringDataCustomRepositoriesAnalyzer extends ByteCodeAnalyzer {

    private final List<String> customRepositoryNames = newArrayList();

    @Override
    protected void analyzeClass(@Nonnull AnalysisContext analysisContext, @Nonnull CtClass clazz) {
        analysisContext.addAnalyzedClass(clazz.getName());
        if (clazz.isInterface()) {
            analyzeInterface(analysisContext, clazz);
        } else if (isPublicOrPackageProtectedClass(clazz)) {
            reportImplementationOfExistingCustomRepository(analysisContext, clazz);
        }
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisContext analysisContext) {
        analysisContext.getCache().put(getClass(), resultSetFor(this.customRepositoryNames));
        this.customRepositoryNames.clear();
    }

    private void analyzeInterface(@Nonnull AnalysisContext analysisContext, @Nonnull CtClass clazz) {
        Set<String> implementedInterfaces = getAllImplementedInterfaces(clazz);
        if (!implementedInterfaces.contains("org.springframework.data.repository.Repository")) {
            return;
        }

        final String nameOfCustomRepositoryInterface = clazz.getName() + "Custom";
        if (!implementedInterfaces.contains(nameOfCustomRepositoryInterface)) {
            return;
        }

        this.customRepositoryNames.add(nameOfCustomRepositoryInterface);
        reportImplementationOfNewCustomRepository(analysisContext, clazz);
    }

    private void reportImplementationOfNewCustomRepository(@Nonnull AnalysisContext analysisContext,
                                                           @Nonnull CtClass clazz) {
        final String clazzName = clazz.getName();
        final String nameOfCustomRepositoryImplementation = clazzName + "Impl";
        CtClass customImpl = clazz.getClassPool().getOrNull(nameOfCustomRepositoryImplementation);
        if (customImpl == null) {
            return;
        }

        Set<String> implementedInterfaces = getAllImplementedInterfaces(customImpl);
        if (implementedInterfaces.contains(clazzName + "Custom")) {
            analysisContext.addDependencies(clazzName, nameOfCustomRepositoryImplementation);
        }
    }

    private boolean isPublicOrPackageProtectedClass(@Nonnull CtClass clazz) {
        int modifiers = clazz.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && !Modifier.isAnnotation(modifiers)
                && !Modifier.isEnum(modifiers)
                && !Modifier.isPrivate(modifiers)
                && !Modifier.isProtected(modifiers);
    }

    private void reportImplementationOfExistingCustomRepository(@Nonnull AnalysisContext analysisContext,
                                                                @Nonnull CtClass clazz) {
        IntermediateResultSet<String> intermediateResults = resultSetFrom(analysisContext, getClass());
        if (intermediateResults == null) {
            return;
        }

        Set<String> existingCustomRepositories = intermediateResults.getResults();
        Set<String> implementedInterfaces = getAllImplementedInterfaces(clazz);
        implementedInterfaces.retainAll(existingCustomRepositories);
        for (String customRepositoryName : implementedInterfaces) {
            analysisContext.addDependencies(
                    customRepositoryName.substring(0, customRepositoryName.length() - "Custom".length()),
                    clazz.getName());
        }
    }

}
