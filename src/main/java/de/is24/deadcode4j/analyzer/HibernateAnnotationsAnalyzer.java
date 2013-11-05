package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Analyzes class files: looks for <code>org.hibernate.annotations.TypeDef</code> and
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
        processTypeAnnotations(clazz);
        reportDependencies(codeContext);
    }

    private void processTypeDefAnnotation(@Nonnull CtClass clazz) {
        @SuppressWarnings("unchecked") List<AttributeInfo> attributes = clazz.getClassFile2().getAttributes();
        Annotation typeDef = getAnnotation(attributes, "org.hibernate.annotations.TypeDef");
        if (typeDef != null) {
            String typeName = ((StringMemberValue) typeDef.getMemberValue("name")).getValue();
            this.typeDefinitions.put(typeName, clazz.getName());
        }
    }

    @Nullable
    private Annotation getAnnotation(@Nonnull List<AttributeInfo> attributes,@Nonnull String typeName) {
        for (AttributeInfo attribute : attributes) {
            if (AnnotationsAttribute.class.isInstance(attribute)) {
                for (Annotation annotation : AnnotationsAttribute.class.cast(attribute).getAnnotations()) {
                    if (typeName.equals(annotation.getTypeName()))
                        return annotation;
                }
            }
        }

        return null;
    }

    private void processTypeAnnotations(@Nonnull CtClass clazz) {
        for (CtField field : clazz.getDeclaredFields()) {
            @SuppressWarnings("unchecked") List<AttributeInfo> attributes = field.getFieldInfo2().getAttributes();
            processMemmerAnnotations(attributes, clazz);
        }

        for (CtMethod method : clazz.getDeclaredMethods()) {
            @SuppressWarnings("unchecked") List<AttributeInfo> attributes = method.getMethodInfo2().getAttributes();
            processMemmerAnnotations(attributes, clazz);
        }

    }

    private void processMemmerAnnotations(List<AttributeInfo> attributes, CtClass clazz) {
        Annotation annotation = getAnnotation(attributes, "org.hibernate.annotations.Type");
        if (annotation != null) {
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
        HashSet<String> reportedTypes = newHashSet();
        for (Map.Entry<String, Collection<String>> typeUsage : this.typeUsages.entrySet()) {
            String typeName = typeUsage.getKey();
            String classDefiningType = this.typeDefinitions.get(typeName);
            if (classDefiningType != null) {
                for (String classUsingType : typeUsage.getValue()) {
                    codeContext.addDependencies(classUsingType, classDefiningType);
                }
                reportedTypes.add(typeName);
            }
        }
        for (String reportedType : reportedTypes) {
            this.typeUsages.remove(reportedType);
        }
    }

}
