package de.is24.deadcode4j.analyzer;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import javassist.CtClass;
import javassist.bytecode.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.newHashMap;
import static de.is24.deadcode4j.Utils.getOrAddMappedSet;
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
public class HibernateAnnotationsAnalyzer extends ByteCodeAnalyzer implements Analyzer {

    private final Map<String, String> typeDefinitions = newHashMap();
    private final Map<String, Set<String>> typeUsages = newHashMap();
    private final Map<String, String> generatorDefinitions = newHashMap();
    private final Map<String, Set<String>> generatorStrategies = newHashMap();
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
        if (memberValue == null)
            return null;
        if (!StringMemberValue.class.isInstance(memberValue))
            throw new IllegalArgumentException("The member [" + memberName + "] is no StringMemberValue!");
        return StringMemberValue.class.cast(memberValue).getValue();
    }

    @Nonnull
    private static Iterable<Annotation> getAnnotationsFrom(@Nonnull Annotation annotation, @Nonnull String memberName) {
        MemberValue memberValue = annotation.getMemberValue(memberName);
        if (memberValue == null)
            return emptyList();
        if (!ArrayMemberValue.class.isInstance(memberValue))
            throw new IllegalArgumentException("The member [" + memberName + "] is no ArrayMemberValue!");
        MemberValue[] nestedMembers = ArrayMemberValue.class.cast(memberValue).getValue();
        return filter(transform(asList(nestedMembers), new Function<MemberValue, Annotation>() {
            @Override
            public Annotation apply(@Nullable MemberValue memberValue) {
                return memberValue == null ? null : ((AnnotationMemberValue) memberValue).getValue();
            }
        }), notNull());
    }

    @Override
    protected final void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        codeContext.addAnalyzedClass(clazz.getName());
        processTypeDefAnnotation(clazz);
        processTypeDefsAnnotation(clazz);
        processTypeAnnotations(clazz);
        processGenericGenerator(clazz);
        processGenericGenerators(clazz);
        processGeneratedValueAnnotations(clazz);
    }

    @Override
    public void finishAnalysis(@Nonnull CodeContext codeContext) {
        reportDependencies(codeContext);
    }

    private void processTypeDefAnnotation(@Nonnull CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.TypeDef", PACKAGE, TYPE)) {
            processTypeDefinition(clazz, annotation);
        }
    }

    private void processTypeDefinition(@Nonnull CtClass clazz, @Nonnull Annotation annotation) {
        String className = clazz.getName();
        String typeName = getStringFrom(annotation, "name");
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
            String typeName = getStringFrom(annotation, "type");
            getOrAddMappedSet(this.typeUsages, typeName).add(clazz.getName());
        }
    }

    private void processGenericGenerator(CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.GenericGenerator", PACKAGE, TYPE, METHOD, FIELD)) {
            processGenericGenerator(clazz, annotation);
        }
    }

    private void processGenericGenerator(CtClass clazz, Annotation annotation) {
        String className = clazz.getName();
        String generatorStrategy = getStringFrom(annotation, "strategy");
        getOrAddMappedSet(this.generatorStrategies, className).add(generatorStrategy);
        String generatorName = getStringFrom(annotation, "name");
        String previousEntry = this.generatorDefinitions.put(generatorName, className);
        if (previousEntry != null) {
            logger.warn("The @GenericGenerator named [{}] is defined both by {} and {}.",
                    generatorName, previousEntry, className);
        }
    }

    private void processGenericGenerators(CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.GenericGenerators", PACKAGE, TYPE)) {
            for (Annotation childAnnotation : getAnnotationsFrom(annotation, "value")) {
                processGenericGenerator(clazz, childAnnotation);
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

    private void reportDependencies(@Nonnull CodeContext codeContext) {
        reportGeneratorStrategies(codeContext);
        reportGeneratorUsage(codeContext);
        reportTypeUsage(codeContext);
    }

    private void reportGeneratorStrategies(CodeContext codeContext) {
        for (Map.Entry<String, Set<String>> generatorStrategies : this.generatorStrategies.entrySet()) {
            String definingClass = generatorStrategies.getKey();
            for (String strategy : generatorStrategies.getValue()) {
                if (codeContext.getAnalyzedCode().getAnalyzedClasses().contains(strategy)) {
                    codeContext.addDependencies(definingClass, strategy);
                }
            }
        }
    }

    private void reportGeneratorUsage(CodeContext codeContext) {
        for (Map.Entry<String, Set<String>> generatorUsage : this.generatorUsages.entrySet()) {
            String generatorName = generatorUsage.getKey();
            String classDefiningGenerator = this.generatorDefinitions.get(generatorName);
            if (classDefiningGenerator != null) {
                for (String classUsingGenerator : generatorUsage.getValue()) {
                    codeContext.addDependencies(classUsingGenerator, classDefiningGenerator);
                }
            }
        }
    }

    private void reportTypeUsage(CodeContext codeContext) {
        for (Map.Entry<String, Set<String>> typeUsage : this.typeUsages.entrySet()) {
            String typeName = typeUsage.getKey();
            String classDefiningType = this.typeDefinitions.get(typeName);

            String dependee = null;
            if (classDefiningType != null) {
                dependee = classDefiningType;
            } else if (codeContext.getAnalyzedCode().getAnalyzedClasses().contains(typeName)) {
                dependee = typeName;
            } // no matter what else, scope is outside of the analyzed project
            if (dependee != null) {
                for (String classUsingType : typeUsage.getValue()) {
                    codeContext.addDependencies(classUsingType, dependee);
                }
            }
        }
    }

}
