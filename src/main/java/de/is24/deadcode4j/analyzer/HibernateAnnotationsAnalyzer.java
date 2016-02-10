package de.is24.deadcode4j.analyzer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import de.is24.deadcode4j.AnalysisContext;
import javassist.CtClass;
import javassist.bytecode.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.newHashMap;
import static de.is24.deadcode4j.IntermediateResults.*;
import static de.is24.deadcode4j.Utils.getOrAddMappedSet;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static java.lang.annotation.ElementType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Analyzes class files:
 * <ul>
 * <li>
 * looks for <code>org.hibernate.annotations.TypeDef</code>(<code>s</code>) and
 * <code>org.hibernate.annotations.Type</code> annotations and
 * <ul>
 * <li>sets up a dependency between the class annotated with <code>@Type</code> and the associated
 * <code>@TypeDef</code> annotated class (i.e. <code>Type.type</code> &rarr; <code>TypeDef.name</code>)
 * </li>
 * <li>sets up a dependency between the class annotated with <code>@Type</code> and the class defined by the annotation
 * (<code>Type.type</code>) if that class is part of the project scope </li>
 * </ul>
 * </li>
 * <li>
 * looks for <code>org.hibernate.annotations.GenericGenerator</code>(<code>s</code>) and
 * <code>javax.persistence.GeneratedValue</code> annotations and
 * <ul>
 * <li>sets up a dependency between the class annotated with <code>@GeneratedValue</code> and the associated
 * <code>@GenericGenerator</code> annotated class (i.e. <code>GeneratedValue.generator</code> &rarr;
 * <code>GenericGenerator.name</code>)</li>
 * <li>sets up a dependency between the class annotated with <code>@GenericGenerator</code> and the class defined by the
 * annotation (<code>GenericGenerator.strategy</code>) if that class is part of the project scope </li>
 * </ul>
 * </li>
 * </ul>
 *
 * @since 1.4
 */
public final class HibernateAnnotationsAnalyzer extends ByteCodeAnalyzer {

    private final Map<String, String> typeDefinitions = newHashMap();
    private final Map<String, Set<String>> typeUsages = newHashMap();
    private final Map<String, String> generatorDefinitions = newHashMap();
    private final Map<String, Set<String>> generatorUsages = newHashMap();

    @Nonnull
    private static Iterable<Annotation> getAnnotations(@Nonnull CtClass clazz, @Nonnull final String typeName, ElementType... elementTypes) {
        return filter(getAnnotations(clazz, elementTypes), new Predicate<Annotation>() {
            @Override
            public boolean apply(@Nullable Annotation annotation) {
                return annotation != null && typeName.equals(annotation.getTypeName());
            }
        });
    }

    @Nullable
    private static String getStringFrom(@Nonnull Annotation annotation, @Nonnull String memberName) {
        MemberValue memberValue = annotation.getMemberValue(memberName);
        if (memberValue == null) {
            return null;
        }
        checkState(StringMemberValue.class.isInstance(memberValue),
                "The member [" + memberName + "] is no StringMemberValue!");
        return StringMemberValue.class.cast(memberValue).getValue();
    }

    @Nonnull
    private static String getMandatoryStringFrom(@Nonnull Annotation annotation, @Nonnull String memberName) {
        String memberValue = getStringFrom(annotation, memberName);
        if (memberValue == null) {
            throw new RuntimeException("Annotation [" + annotation.getTypeName()
                    + "] has no value for mandatory member [" + memberName + "]!");
        }
        return memberValue;
    }


    @Nonnull
    private static Iterable<Annotation> getAnnotationsFrom(@Nonnull Annotation annotation, @Nonnull String memberName) {
        MemberValue memberValue = annotation.getMemberValue(memberName);
        if (memberValue == null) {
            return emptyList();
        }
        checkState(ArrayMemberValue.class.isInstance(memberValue),
                "The member [" + memberName + "] is no ArrayMemberValue!");
        MemberValue[] nestedMembers = ArrayMemberValue.class.cast(memberValue).getValue();
        return filter(transform(asList(nestedMembers), new Function<MemberValue, Annotation>() {
            @Override
            public Annotation apply(@Nullable MemberValue memberValue) {
                return memberValue == null ? null : AnnotationMemberValue.class.cast(memberValue).getValue();
            }
        }), notNull());
    }

    @Override
    protected void analyzeClass(@Nonnull AnalysisContext analysisContext, @Nonnull CtClass clazz) {
        analysisContext.addAnalyzedClass(clazz.getName());
        processTypeDefAnnotation(clazz);
        processTypeDefsAnnotation(clazz);
        processTypeAnnotations(clazz);
        processGenericGenerator(analysisContext, clazz);
        processGenericGenerators(analysisContext, clazz);
        processGeneratedValueAnnotations(clazz);
    }

    @Override
    public void finishAnalysis(@Nonnull AnalysisContext analysisContext) {
        reportDependencies(analysisContext);
        storeIntermediateResults(analysisContext);
    }

    private void processTypeDefAnnotation(@Nonnull CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.TypeDef", PACKAGE, TYPE)) {
            processTypeDefinition(clazz, annotation);
        }
    }

    private void processTypeDefinition(@Nonnull CtClass clazz, @Nonnull Annotation annotation) {
        String typeName = getStringFrom(annotation, "name");
        if (typeName == null) {
            return;
        }
        String className = clazz.getName();
        String previousEntry = this.typeDefinitions.put(typeName, className);
        if (previousEntry != null) {
            logger.warn("The @TypeDef named [{}] is defined both by {} and {}.", typeName, previousEntry, className);
        }
    }

    private void processTypeDefsAnnotation(@Nonnull CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.TypeDefs", PACKAGE, TYPE)) {
            for (Annotation childAnnotation : getAnnotationsFrom(annotation, "value")) {
                processTypeDefinition(clazz, childAnnotation);
            }
        }
    }

    private void processTypeAnnotations(@Nonnull CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.Type", METHOD, FIELD)) {
            String typeName = getMandatoryStringFrom(annotation, "type");
            getOrAddMappedSet(this.typeUsages, typeName).add(clazz.getName());
        }
    }

    private void processGenericGenerator(AnalysisContext analysisContext, CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.GenericGenerator", PACKAGE, TYPE, METHOD, FIELD)) {
            processGenericGenerator(analysisContext, clazz, annotation);
        }
    }

    private void processGenericGenerator(AnalysisContext analysisContext, CtClass clazz, Annotation annotation) {
        String className = clazz.getName();
        Optional<String> resolvedStrategyClass = classPoolAccessorFor(analysisContext).resolveClass(
                getMandatoryStringFrom(annotation, "strategy"));
        if (resolvedStrategyClass.isPresent()) {
            analysisContext.addDependencies(className, resolvedStrategyClass.get());
        }
        String generatorName = getMandatoryStringFrom(annotation, "name");
        String previousEntry = this.generatorDefinitions.put(generatorName, className);
        if (previousEntry != null) {
            logger.warn("The @GenericGenerator named [{}] is defined both by {} and {}.",
                    generatorName, previousEntry, className);
        }
    }

    private void processGenericGenerators(AnalysisContext analysisContext, CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.GenericGenerators", PACKAGE, TYPE)) {
            for (Annotation childAnnotation : getAnnotationsFrom(annotation, "value")) {
                processGenericGenerator(analysisContext, clazz, childAnnotation);
            }
        }
    }

    private void processGeneratedValueAnnotations(CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "javax.persistence.GeneratedValue", METHOD, FIELD)) {
            String generatorName = getStringFrom(annotation, "generator");
            if (generatorName != null) {
                getOrAddMappedSet(this.generatorUsages, generatorName).add(clazz.getName());
            }
        }
    }

    private void reportDependencies(@Nonnull AnalysisContext analysisContext) {
        reportNewGeneratorUsages(analysisContext);
        reportExistingGeneratorUsagesForNewDefinitions(analysisContext);
        reportNewTypeUsages(analysisContext);
        reportExistingTypeUsagesForNewDefinitions(analysisContext);
    }

    private void reportNewGeneratorUsages(AnalysisContext analysisContext) {
        if (this.generatorUsages.isEmpty()) {
            return;
        }
        Map<String, String> allGeneratorDefinitions = getAllGeneratorDefinitions(analysisContext);
        for (Map.Entry<String, Set<String>> generatorUsage : this.generatorUsages.entrySet()) {
            String generatorName = generatorUsage.getKey();
            String classDefiningGenerator = allGeneratorDefinitions.get(generatorName);
            if (classDefiningGenerator != null) {
                for (String classUsingGenerator : generatorUsage.getValue()) {
                    analysisContext.addDependencies(classUsingGenerator, classDefiningGenerator);
                }
            }
        }
    }

    private void reportExistingGeneratorUsagesForNewDefinitions(AnalysisContext analysisContext) {
        if (this.generatorDefinitions.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Set<String>> usage : getExistingGeneratorUsages(analysisContext).entrySet()) {
            String usageName = usage.getKey();
            String classDefiningType = this.generatorDefinitions.get(usageName);
            if (classDefiningType == null) {
                continue;
            }
            logger.debug("This module provides the generator definition [{}] for modules it depends on.", usageName);
            for (String classUsingType : usage.getValue()) {
                analysisContext.addDependencies(classUsingType, classDefiningType);
            }
        }
    }

    private void reportNewTypeUsages(AnalysisContext analysisContext) {
        if (this.typeUsages.isEmpty()) {
            return;
        }
        Map<String, String> allTypeDefinitions = getAllTypeDefinitions(analysisContext);
        for (Map.Entry<String, Set<String>> typeUsage : this.typeUsages.entrySet()) {
            String typeName = typeUsage.getKey();
            String classDefiningType = allTypeDefinitions.get(typeName);

            final String dependee;
            if (classDefiningType != null) {
                dependee = classDefiningType;
            } else {
                Optional<String> resolvedTypeClass = classPoolAccessorFor(analysisContext).resolveClass(typeName);
                if (resolvedTypeClass.isPresent()) {
                    dependee = resolvedTypeClass.get();
                } else {
                    logger.debug("Encountered unknown org.hibernate.annotations.Type [{}].", typeName);
                    continue;
                }
            }
            for (String classUsingType : typeUsage.getValue()) {
                analysisContext.addDependencies(classUsingType, dependee);
            }
        }
    }

    private void reportExistingTypeUsagesForNewDefinitions(AnalysisContext analysisContext) {
        if (this.typeDefinitions.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Set<String>> typeUsage : getExistingTypeUsages(analysisContext).entrySet()) {
            String typeName = typeUsage.getKey();
            String classDefiningType = this.typeDefinitions.get(typeName);
            if (classDefiningType == null) {
                continue;
            }
            logger.debug("This module provides the type definition [{}] for modules it depends on.", typeName);
            for (String classUsingType : typeUsage.getValue()) {
                analysisContext.addDependencies(classUsingType, classDefiningType);
            }
        }
    }

    @Nonnull
    private Map<String, String> getAllGeneratorDefinitions(@Nonnull AnalysisContext analysisContext) {
        IntermediateResultMap<String, String> resultMap =
                resultMapFrom(analysisContext, getClass().getName() + "|generatorDefinitions");
        if (resultMap == null) {
            return this.generatorDefinitions;
        }
        Map<String, String> inheritedDefinitions = resultMap.getResults();

        Map<String, String> allDefinitions = newHashMap(this.generatorDefinitions);
        for (Map.Entry<String, String> inheritedDefinition : inheritedDefinitions.entrySet()) {
            String definitionName = inheritedDefinition.getKey();
            if (allDefinitions.containsKey(definitionName)) {
                logger.debug("The inherited generator definition [{}] is overridden by this module.", definitionName);
                continue;
            }
            allDefinitions.put(definitionName, inheritedDefinition.getValue());
        }

        return allDefinitions;
    }

    @Nonnull
    private Map<String, String> getAllTypeDefinitions(@Nonnull AnalysisContext analysisContext) {
        IntermediateResultMap<String, String> resultMap =
                resultMapFrom(analysisContext, getClass().getName() + "|typeDefinitions");
        if (resultMap == null) {
            return this.typeDefinitions;
        }
        Map<String, String> inheritedTypeDefinitions = resultMap.getResults();

        Map<String, String> allTypeDefinitions = newHashMap(this.typeDefinitions);
        for (Map.Entry<String, String> inheritedDefinition : inheritedTypeDefinitions.entrySet()) {
            String typeName = inheritedDefinition.getKey();
            if (allTypeDefinitions.containsKey(typeName)) {
                logger.debug("The inherited type definition [{}] is overridden by this module.", typeName);
                continue;
            }
            allTypeDefinitions.put(typeName, inheritedDefinition.getValue());
        }

        return allTypeDefinitions;
    }

    @Nonnull
    private Map<String, Set<String>> getExistingGeneratorUsages(@Nonnull AnalysisContext analysisContext) {
        IntermediateResultMap<String, Set<String>> resultMap =
                resultMapFrom(analysisContext, getClass().getName() + "|generatorUsages");
        return resultMap != null ? resultMap.getResults() : Collections.<String, Set<String>>emptyMap();
    }

    @Nonnull
    private Map<String, Set<String>> getExistingTypeUsages(@Nonnull AnalysisContext analysisContext) {
        IntermediateResultMap<String, Set<String>> resultMap =
                resultMapFrom(analysisContext, getClass().getName() + "|typeUsages");
        return resultMap != null ? resultMap.getResults() : Collections.<String, Set<String>>emptyMap();
    }

    private void storeIntermediateResults(@Nonnull AnalysisContext analysisContext) {
        if (!this.generatorDefinitions.isEmpty()) {
            analysisContext.getCache().put(getClass().getName() + "|generatorDefinitions", resultMapFor(this.generatorDefinitions));
            this.generatorDefinitions.clear();
        }
        if (!this.generatorUsages.isEmpty()) {
            analysisContext.getCache().put(getClass().getName() + "|generatorUsages", resultMapFor(this.generatorUsages));
            this.generatorUsages.clear();
        }
        if (!this.typeDefinitions.isEmpty()) {
            analysisContext.getCache().put(getClass().getName() + "|typeDefinitions", resultMapFor(this.typeDefinitions));
            this.typeDefinitions.clear();
        }
        if (!this.typeUsages.isEmpty()) {
            analysisContext.getCache().put(getClass().getName() + "|typeUsages", resultMapFor(this.typeUsages));
            this.typeUsages.clear();
        }
    }

}
