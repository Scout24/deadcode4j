package de.is24.deadcode4j.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Optional;
import de.is24.deadcode4j.AnalysisContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.emptyIfNull;
import static de.is24.deadcode4j.Utils.getOrAddMappedSet;
import static de.is24.javaparser.Nodes.getTypeName;
import static java.util.Collections.emptySet;

/**
 * Analyzes Java files and reports dependencies to classes that are not part of the byte code due to type erasure.
 *
 * @since 2.0.0
 */
public class TypeErasureAnalyzer extends JavaFileAnalyzer {

    @Nonnull
    private static String getFullQualifier(@Nonnull ClassOrInterfaceType classOrInterfaceType) {
        StringBuilder buffy = new StringBuilder(classOrInterfaceType.getName());
        while ((classOrInterfaceType = classOrInterfaceType.getScope()) != null) {
            buffy.insert(0, '.');
            buffy.insert(0, classOrInterfaceType.getName());
        }
        return buffy.toString();
    }

    @Override
    protected void analyzeCompilationUnit(@Nonnull final AnalysisContext analysisContext, @Nonnull final CompilationUnit compilationUnit) {
        compilationUnit.accept(new TypeParameterRecordingVisitor<Void>() {
            private final Map<String, Set<String>> processedReferences = newHashMap();

            @Override
            public void visit(ClassOrInterfaceType n, Void arg) {
                for (Type type : emptyIfNull(n.getTypeArgs())) {
                    ClassOrInterfaceType referencedType = getReferencedType(type);
                    if (referencedType == null) {
                        continue;
                    }
                    if (typeParameterWithSameNameIsDefined(referencedType)) {
                        continue;
                    }
                    resolveTypeReference(referencedType);
                    this.visit(referencedType, arg); // resolve nested type arguments
                }
            }

            @Nullable
            private ClassOrInterfaceType getReferencedType(@Nonnull Type type) {
                final Type nestedType;
                if (ReferenceType.class.isInstance(type)) {
                    nestedType = ReferenceType.class.cast(type).getType();
                } else if (WildcardType.class.isInstance(type)) {
                    WildcardType wildcardType = WildcardType.class.cast(type);
                    ReferenceType referenceType = wildcardType.getExtends();
                    if (referenceType == null) {
                        referenceType = wildcardType.getSuper();
                    }
                    if (referenceType == null) {
                        // unbounded wildcard - nothing ro refer to
                        return null;
                    }
                    nestedType = referenceType.getType();
                } else {
                    logger.warn("Encountered unexpected Type [{}:{}]; please create an issue at https://github.com/ImmobilienScout24/deadcode4j.", type.getClass(), type);
                    return null;
                }
                if (PrimitiveType.class.isInstance(nestedType)) {
                    // references to primitives won't be reported
                    return null;
                }
                if (!ClassOrInterfaceType.class.isInstance(nestedType)) {
                    logger.warn("[{}:{}] is no ClassOrInterfaceType; please create an issue at https://github.com/ImmobilienScout24/deadcode4j.", type.getClass(), type);
                    return null;
                }
                return ClassOrInterfaceType.class.cast(nestedType);
            }

            private void resolveTypeReference(final ClassOrInterfaceType referencedType) {
                if (!needsProcessing(referencedType)) {
                    return;
                }
                Optional<String> resolvedClass = resolveType(analysisContext,
                        new ClassOrInterfaceTypeQualifier(referencedType));
                String depender = getTypeName(referencedType);
                if (resolvedClass.isPresent()) {
                    analysisContext.addDependencies(depender, resolvedClass.get());
                } else {
                    logger.debug("Could not resolve Type Argument [{}] used by [{}].",
                            getFullQualifier(referencedType), depender);
                }
            }

            private boolean needsProcessing(ClassOrInterfaceType referencedType) {
                Set<String> references = getOrAddMappedSet(this.processedReferences, getTypeName(referencedType));
                return references.add(getFullQualifier(referencedType));
            }

        }, null);
    }

    private static class TypeParameterRecordingVisitor<A> extends VoidVisitorAdapter<A> {
        private final Deque<Set<String>> definedTypeParameters = newLinkedList();

        @Override
        public void visit(ClassOrInterfaceDeclaration n, A arg) {
            this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
            try {
                super.visit(n, arg);
            } finally {
                this.definedTypeParameters.removeLast();
            }
        }

        @Override
        public void visit(ConstructorDeclaration n, A arg) {
            this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
            try {
                super.visit(n, arg);
            } finally {
                this.definedTypeParameters.removeLast();
            }
        }

        @Override
        public void visit(MethodDeclaration n, A arg) {
            this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
            try {
                super.visit(n, arg);
            } finally {
                this.definedTypeParameters.removeLast();
            }
        }

        @Override
        public void visit(MethodReferenceExpr n, A arg) {
            this.definedTypeParameters.addLast(getTypeParameterNames(n.getTypeParameters()));
            try {
                super.visit(n, arg);
            } finally {
                this.definedTypeParameters.removeLast();
            }
        }

        protected boolean typeParameterWithSameNameIsDefined(@Nonnull ClassOrInterfaceType nestedClassOrInterface) {
            if (nestedClassOrInterface.getScope() != null) {
                return false;
            }
            for (Set<String> definedTypeNames : this.definedTypeParameters) {
                if (definedTypeNames.contains(nestedClassOrInterface.getName())) {
                    return true;
                }
            }
            return false;
        }

        @Nonnull
        private Set<String> getTypeParameterNames(@Nullable List<TypeParameter> typeParameters) {
            if (typeParameters == null) {
                return emptySet();
            }
            Set<String> parameters = newHashSet();
            for (TypeParameter typeParameter : typeParameters) {
                parameters.add(typeParameter.getName());
            }
            return parameters;
        }

    }

    private static class ClassOrInterfaceTypeQualifier extends Qualifier<ClassOrInterfaceType> {

        public ClassOrInterfaceTypeQualifier(ClassOrInterfaceType referencedType) {
            super(referencedType, null);
        }

        private ClassOrInterfaceTypeQualifier(ClassOrInterfaceType referencedType,
                                              ClassOrInterfaceTypeQualifier parent) {
            super(referencedType, parent);
        }

        @Nonnull
        @Override
        protected String getName(@Nonnull ClassOrInterfaceType referencedType) {
            return referencedType.getName();
        }

        @Nonnull
        @Override
        protected String getFullQualifier(@Nonnull ClassOrInterfaceType referencedType) {
            return TypeErasureAnalyzer.getFullQualifier(referencedType);
        }

        @Nullable
        @Override
        protected Qualifier getScopeQualifier(@Nonnull ClassOrInterfaceType referencedType) {
            ClassOrInterfaceType scope = referencedType.getScope();
            return scope == null ? null : new ClassOrInterfaceTypeQualifier(scope, this);
        }

        @Override
        public boolean allowsPartialResolving() {
            return false;
        }

    }

}
