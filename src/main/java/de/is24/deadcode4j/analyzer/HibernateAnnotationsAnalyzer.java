package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Analyzes class files: looks for <code>org.hibernate.annotations.TypeDef</code> annotations and
 * <ul>
 *     <li>sets up a dependency from the <i><code>typeClass</code></i> to the annotated class</li>
 *     <li>sets up a dependency from classes having a field or method being annotated with
 *     <code>org.hibernate.annotations.Type</code> to the associated <i><code>typeClass</code></i></li>
 * </ul>
 *
 * @since 1.4
 */
public class HibernateAnnotationsAnalyzer extends ByteCodeAnalyzer implements Analyzer {

    @Override
    protected final void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
        String className = clazz.getName();
        codeContext.addAnalyzedClass(className);

        Annotation typeDef = getAnnotation(clazz, "org.hibernate.annotations.TypeDef");
        if (typeDef == null)
            return;
        String typeClass = ((ClassMemberValue) typeDef.getMemberValue("typeClass")).getValue();
        codeContext.addDependencies(typeClass, className);
    }

    @Nullable
    private Annotation getAnnotation(@Nonnull CtClass ctClass, @Nonnull String typeName) {
        @SuppressWarnings("unchecked") List<AttributeInfo> attributes = ctClass.getClassFile2().getAttributes();

        for (AttributeInfo attribute : attributes) {
            if (!AnnotationsAttribute.class.isInstance(attribute)) {
                continue;
            }
            for (Annotation annotation : AnnotationsAttribute.class.cast(attribute).getAnnotations()) {
                if (typeName.equals(annotation.getTypeName()))
                    return annotation;
            }
        }

        return null;
    }

}
