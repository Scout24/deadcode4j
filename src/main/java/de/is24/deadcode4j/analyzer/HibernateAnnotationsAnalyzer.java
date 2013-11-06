package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.*;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.annotation.ElementType.*;
import static java.util.Arrays.asList;

/**
 * Analyzes class files: looks for
 * <code>org.hibernate.annotations.TypeDef</code>/<code>org.hibernate.annotations.TypeDefs</code> and
 * <code>org.hibernate.annotations.Type</code> annotations and sets up a dependency between the class annotated with
 * <code>@Type</code> and the associated <code>@TypeDef</code> annotated class
 * (i.e. <code>Type.type</code> &rarr; <code>TypeDef.name</code>).
 *
 * @since 1.4
 */
public class HibernateAnnotationsAnalyzer extends ByteCodeAnalyzer implements Analyzer {

    private final Map<String, String> typeDefinitions = newHashMap();
    private Map<String, Collection<String>> typeUsages = newHashMap();

    @Override
    protected final void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        codeContext.addAnalyzedClass(clazz.getName());
        processTypeDefAnnotation(clazz);
        processTypeDefsAnnotation(clazz);
        processTypeAnnotations(clazz);
    }

    @Override
    public void finishAnalysis(@Nonnull CodeContext codeContext) {
        reportDependencies(codeContext);
    }

    private void processTypeDefAnnotation(@Nonnull CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.TypeDef", TYPE)) {
            reportTypeDefinition(clazz, annotation);
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private Collection<Annotation> getAnnotations(@Nonnull CtClass clazz, @Nonnull String typeName, ElementType... elementTypes) {
        List<ElementType> types = asList(elementTypes);
        List<AttributeInfo> attributes = newArrayList();
        if (types.contains(ElementType.TYPE)) {
            attributes.addAll(clazz.getClassFile2().getAttributes());
        }
        if (types.contains(METHOD)) {
            for (CtMethod method : clazz.getDeclaredMethods()) {
                attributes.addAll(method.getMethodInfo2().getAttributes());
            }
        }
        if (types.contains(FIELD)) {
            for (CtField field : clazz.getDeclaredFields()) {
                attributes.addAll(field.getFieldInfo2().getAttributes());
            }
        }

        List<Annotation> annotations = newArrayList();
        for (AttributeInfo attribute : attributes) {
            if (AnnotationsAttribute.class.isInstance(attribute)) {
                for (Annotation annotation : AnnotationsAttribute.class.cast(attribute).getAnnotations()) {
                    if (typeName.equals(annotation.getTypeName()))
                        annotations.add(annotation);
                }
            }
        }

        return annotations;
    }

    private void reportTypeDefinition(@Nonnull CtClass clazz, @Nonnull Annotation annotation) {
        String typeName = ((StringMemberValue) annotation.getMemberValue("name")).getValue();
        this.typeDefinitions.put(typeName, clazz.getName());
    }

    private void processTypeDefsAnnotation(@Nonnull CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.TypeDefs", TYPE)) {
            for (MemberValue memberValue : ((ArrayMemberValue) annotation.getMemberValue("value")).getValue()) {
                reportTypeDefinition(clazz, ((AnnotationMemberValue) memberValue).getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processTypeAnnotations(@Nonnull CtClass clazz) {
        for (Annotation annotation : getAnnotations(clazz, "org.hibernate.annotations.Type", METHOD, FIELD)) {
            String typeName = ((StringMemberValue) annotation.getMemberValue("type")).getValue();
            reportTypeUsage(clazz, typeName);
        }
    }

    private void reportTypeUsage(@Nonnull CtClass clazz, @Nonnull String typeName) {
        Collection<String> classesUsingType = this.typeUsages.get(typeName);
        if (classesUsingType == null) {
            classesUsingType = newHashSet();
            Collection<String> previous = this.typeUsages.put(typeName, classesUsingType);
            if (previous != null) {
                classesUsingType.addAll(previous);
            }
        }
        classesUsingType.add(clazz.getName());
    }

    private void reportDependencies(@Nonnull CodeContext codeContext) {
        for (Map.Entry<String, Collection<String>> typeUsage : this.typeUsages.entrySet()) {
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
