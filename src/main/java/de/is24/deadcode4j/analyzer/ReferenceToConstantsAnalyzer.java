package de.is24.deadcode4j.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.*;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static de.is24.javaparser.ImportDeclarations.*;
import static de.is24.javaparser.Nodes.getTypeName;
import static de.is24.javassist.CtClasses.*;

/**
 * Analyzes Java files and reports dependencies to classes that are not part of the byte code due to constant inlining.
 * <b>Note</b> that this analyzer does not distinguish between inlined and referenced constants.
 *
 * @since 2.0.0
 */
public class ReferenceToConstantsAnalyzer extends JavaFileAnalyzer {

    @Nonnull
    private static String getFirstElement(@Nonnull FieldAccessExpr fieldAccessExpr) {
        return getFirstNode(fieldAccessExpr).getName();
    }

    @Nonnull
    private static NameExpr getFirstNode(@Nonnull FieldAccessExpr fieldAccessExpr) {
        Expression scope = fieldAccessExpr.getScope();
        if (NameExpr.class.isInstance(scope)) {
            return NameExpr.class.cast(scope);
        } else if (FieldAccessExpr.class.isInstance(scope)) {
            return getFirstNode(FieldAccessExpr.class.cast(scope));
        }
        throw new RuntimeException("Should not have reached this point!");
    }

    private static boolean isRegularFieldAccessExpr(@Nonnull FieldAccessExpr fieldAccessExpr) {
        Expression scope = fieldAccessExpr.getScope();
        if (NameExpr.class.isInstance(scope)) {
            return true;
        } else if (FieldAccessExpr.class.isInstance(scope)) {
            return isRegularFieldAccessExpr(FieldAccessExpr.class.cast(scope));
        }
        return false;
    }

    private static String getFullQualifier(FieldAccessExpr reference) {
        StringBuilder buffy = new StringBuilder(reference.getField());
        for (FieldAccessExpr loop = reference; loop != null; ) {
            Expression scope = loop.getScope();
            final String qualifier;
            if (NameExpr.class.isInstance(scope)) {
                loop = null;
                qualifier = NameExpr.class.cast(scope).getName();
            } else {
                loop = FieldAccessExpr.class.cast(scope);
                qualifier = loop.getField();
            }
            buffy.insert(0, '.');
            buffy.insert(0, qualifier);
        }
        return buffy.toString();
    }

    private static boolean isScopeOfAMethodCall(@Nonnull Expression expression) {
        return MethodCallExpr.class.isInstance(expression.getParentNode())
                && expression == MethodCallExpr.class.cast(expression.getParentNode()).getScope();
    }

    private static boolean isScopeOfThisExpression(@Nonnull Expression expression) {
        return ThisExpr.class.isInstance(expression.getParentNode());
    }

    private static boolean isTargetOfAnAssignment(@Nonnull Expression expression) {
        return AssignExpr.class.isInstance(expression.getParentNode())
                && expression == AssignExpr.class.cast(expression.getParentNode()).getTarget();
    }

    private static Function<? super ImportDeclaration, ? extends String> toImportedType() {
        return new Function<ImportDeclaration, String>() {
            @Nullable
            @Override
            public String apply(@Nullable ImportDeclaration input) {
                if (input == null) {
                    return null;
                }
                NameExpr name = input.getName();
                if (input.isStatic() && !input.isAsterisk()) {
                    name = QualifiedNameExpr.class.cast(name).getQualifier();
                }
                return name.toString();
            }
        };
    }

    private static boolean isConstant(CtField ctField) {
        return Modifier.isStatic(ctField.getModifiers()) && Modifier.isFinal(ctField.getModifiers());
    }

    @Override
    protected void analyzeCompilationUnit(@Nonnull final AnalysisContext analysisContext,
                                          @Nonnull final CompilationUnit compilationUnit) {
        compilationUnit.accept(new LocalVariableRecordingVisitor<Void>() {
            private final ClassPoolAccessor classPoolAccessor = classPoolAccessorFor(analysisContext);
            private final Map<String, Set<String>> processedReferences = newHashMap();

            @Override
            public void visit(FieldAccessExpr n, Void arg) {
                if (isTargetOfAnAssignment(n)
                        || isScopeOfThisExpression(n)) {
                    return;
                }
                if (!isRegularFieldAccessExpr(n)) {
                    super.visit(n, arg);
                    return;
                }
                if (aLocalVariableExists(getFirstElement(n))) {
                    return;
                }
                resolveFieldReference(n);
            }

            @Override
            public void visit(NameExpr n, Void arg) {
                if (isTargetOfAnAssignment(n)
                        || isScopeOfThisExpression(n)
                        || aLocalVariableExists(n.getName())) {
                    return;
                }
                resolveNameReference(n);
            }

            private Optional<String> resolveClass(String qualifier) {
                return this.classPoolAccessor.resolveClass(qualifier);
            }

            private void resolveFieldReference(FieldAccessExpr reference) {
                if (!needsProcessing(reference)) {
                    return;
                }
                Optional<String> resolvedType;
                if (isScopeOfAMethodCall(reference)) {
                    FieldAccessExprQualifier qualifier = new FieldAccessExprQualifier(reference);
                    resolvedType = resolveType(analysisContext, qualifier);
                    if (resolvedType.isPresent() && isFullyResolved(resolvedType.get(), qualifier)) {
                        return; // this is just a static method
                    }
                } else {
                    resolvedType = resolveType(analysisContext, qualifierFor(reference));
                }

                String referencingType = getTypeName(reference);
                if (resolvedType.isPresent()) {
                    analysisContext.addDependencies(referencingType, resolvedType.get());
                } else {
                    logger.debug("Could not resolve reference [{}] found within [{}].",
                            reference, referencingType);
                }
            }

            private boolean needsProcessing(FieldAccessExpr fieldAccessExpr) {
                Set<String> references = getOrAddMappedSet(this.processedReferences, getTypeName(fieldAccessExpr));
                return references.add(getFullQualifier(fieldAccessExpr));
            }

            private Qualifier qualifierFor(FieldAccessExpr fieldAccessExpr) {
                Expression scope = fieldAccessExpr.getScope();
                return NameExpr.class.isInstance(scope)
                        ? new NameExprQualifier(NameExpr.class.cast(scope))
                        : new FieldAccessExprQualifier(FieldAccessExpr.class.cast(scope));
            }

            private void resolveNameReference(NameExpr reference) {
                if (!needsProcessing(reference)) {
                    return;
                }
                if (isScopeOfAMethodCall(reference)
                        && resolveType(analysisContext, new NameExprQualifier(reference)).isPresent()) {
                    return; // this is just a static method call
                }
                if (refersToInheritedField(reference)
                        || refersToStaticImport(reference)
                        || refersToAsteriskStaticImport(reference)) {
                    return;
                }
                if (SwitchEntryStmt.class.isInstance(reference.getParentNode())) {
                    return; // see A_ReferenceToConstantsAnalyzer#recognizesReferenceToEnumerationInSwitch()
                }
                logger.debug("Could not resolve name reference [{}] found within [{}].",
                        reference, getTypeName(reference));
            }

            private boolean needsProcessing(NameExpr nameExpr) {
                Set<String> references = getOrAddMappedSet(this.processedReferences, getTypeName(nameExpr));
                return references.add(nameExpr.getName());
            }

            private boolean refersToInheritedField(NameExpr reference) {
                CtClass referencingClazz = getCtClass(classPoolAccessor.getClassPool(), getTypeName(reference));
                if (referencingClazz == null) {
                    return false;
                }
                for (CtClass declaringClazz : getDeclaringClassesOf(referencingClazz)) {
                    if (refersToInheritedField(referencingClazz, declaringClazz, reference)) {
                        return true;
                    }
                }
                return false;
            }

            private boolean refersToInheritedField(@Nonnull final CtClass referencingClazz,
                                                   @Nullable CtClass clazz,
                                                   @Nonnull final NameExpr reference) {
                if (clazz == null || isJavaLangObject(clazz)) {
                    return false;
                }
                for (CtField ctField : clazz.getDeclaredFields()) {
                    if (ctField.getName().equals(reference.getName()) && fieldIsVisibleFrom(ctField, referencingClazz)) {
                        if (isConstant(ctField)) { // we only care for static references
                            analysisContext.addDependencies(referencingClazz.getName(), clazz.getName());
                        }
                        return true;
                    }
                }
                if (refersToInheritedField(referencingClazz, getSuperclassOf(clazz), reference)) {
                    return true;
                }
                for (CtClass interfaceClazz : getInterfacesOf(clazz)) {
                    if (refersToInheritedField(referencingClazz, interfaceClazz, reference)) {
                        return true;
                    }
                }
                return false;
            }

            /**
             * Unfortunately, {@link CtField#visibleFrom(CtClass)} is not entirely correct, as anonymous and inner
             * classes have access to private fields of the declaring class.
             */
            private boolean fieldIsVisibleFrom(CtField ctField, CtClass referencingClazz) {
                return ctField.visibleFrom(referencingClazz) ||
                        (isNestedClass(referencingClazz, ctField.getDeclaringClass())
                                && (isStaticField(ctField) || !isStaticClass(referencingClazz)));
            }

            private boolean isNestedClass(CtClass nestedClass, CtClass parentClass) {
                return nestedClass.getName().startsWith(parentClass.getName() + "$");
            }

            private boolean isStaticClass(CtClass referencingClazz) {
                return Modifier.isStatic(referencingClazz.getModifiers());
            }

            private boolean isStaticField(CtField ctField) {
                return Modifier.isStatic(ctField.getModifiers());
            }

            private boolean refersToStaticImport(NameExpr reference) {
                String referenceName = reference.getName();
                String staticImport = getStaticImport(referenceName);
                if (staticImport == null) {
                    return false;
                }
                String typeName = getTypeName(reference);
                Optional<String> resolvedClass = resolveClass(staticImport);
                if (resolvedClass.isPresent()) {
                    analysisContext.addDependencies(typeName, resolvedClass.get());
                } else {
                    logger.warn("Could not resolve static import [{}.{}] found within [{}]!",
                            staticImport, referenceName, typeName);
                }
                return true;
            }

            private boolean refersToAsteriskStaticImport(NameExpr reference) {
                CtClass referencingClazz = getCtClass(classPoolAccessor.getClassPool(), getTypeName(reference));
                if (referencingClazz == null) {
                    return false;
                }
                for (String asteriskImport : getStaticAsteriskImports()) {
                    Optional<String> resolvedClass = resolveClass(asteriskImport);
                    if (!resolvedClass.isPresent()) {
                        String typeName = getTypeName(reference);
                        logger.warn("Could not resolve static import [{}.*] found within [{}]!",
                                asteriskImport, typeName);
                        continue;
                    }
                    CtClass potentialTarget = getCtClass(classPoolAccessor.getClassPool(), resolvedClass.get());
                    if (refersToInheritedField(referencingClazz, potentialTarget, reference)) {
                        return true;
                    }
                }

                return false;
            }

            @Nullable
            @SuppressWarnings("unchecked")
            private String getStaticImport(String referenceName) {
                return getOnlyElement(transform(filter(emptyIfNull(compilationUnit.getImports()),
                        and(refersTo(referenceName), not(isAsterisk()), isStatic())), toImportedType()), null);
            }

            @Nonnull
            private Iterable<String> getStaticAsteriskImports() {
                return transform(filter(emptyIfNull(compilationUnit.getImports()),
                        and(isAsterisk(), isStatic())), toImportedType());
            }

            @Override
            public void visit(ClassOrInterfaceType n, Void arg) {
                // performance
            }

            @Override
            public void visit(CompilationUnit n, Void arg) {
                // performance
                for (final TypeDeclaration typeDeclaration : emptyIfNull(n.getTypes())) {
                    typeDeclaration.accept(this, arg);
                }
            }

            @Override
            public void visit(MarkerAnnotationExpr n, Void arg) {
                // performance
            }

            @Override
            public void visit(MethodReferenceExpr n, Void arg) {
                // performance; only possible scope is TypeExpr - and types are irrelevant
            }

            @Override
            public void visit(NormalAnnotationExpr n, Void arg) {
                // performance
                for (final MemberValuePair m : emptyIfNull(n.getPairs())) {
                    m.accept(this, arg);
                }
            }

            @Override
            public void visit(Parameter n, Void arg) {
                // performance
            }

            @Override
            public void visit(SingleMemberAnnotationExpr n, Void arg) {
                // performance
                n.getMemberValue().accept(this, arg);
            }

        }, null);
    }

    private static class LocalVariableRecordingVisitor<A> extends VoidVisitorAdapter<A> {

        @Nonnull
        private final Deque<Set<String>> localVariables = newLinkedList();

        private static Predicate<? super FieldDeclaration> constants() {
            return new Predicate<FieldDeclaration>() {
                @Override
                @SuppressWarnings("ConstantConditions")
                public boolean apply(@Nullable FieldDeclaration fieldDeclaration) {
                    int modifiers = checkNotNull(fieldDeclaration).getModifiers();
                    return ModifierSet.isStatic(modifiers) && ModifierSet.isFinal(modifiers);
                }
            };
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, A arg) {
            HashSet<String> fields = newHashSet();
            this.localVariables.addLast(fields);
            try {
                addFieldVariables(n, fields);
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(EnumDeclaration n, A arg) {
            HashSet<String> fieldsAndEnums = newHashSet();
            this.localVariables.addLast(fieldsAndEnums);
            try {
                for (EnumConstantDeclaration enumConstantDeclaration : emptyIfNull(n.getEntries())) {
                    fieldsAndEnums.add(enumConstantDeclaration.getName());
                }
                addFieldVariables(n, fieldsAndEnums);
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(ObjectCreationExpr n, A arg) {
            HashSet<String> fields = newHashSet();
            this.localVariables.addLast(fields);
            try {
                addFieldVariables(n.getAnonymousClassBody(), fields);
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(ConstructorDeclaration n, A arg) {
            HashSet<String> blockVariables = newHashSet();
            this.localVariables.addLast(blockVariables);
            try {
                for (Parameter parameter : emptyIfNull(n.getParameters())) {
                    blockVariables.add(parameter.getId().getName());
                }
                for (AnnotationExpr annotationExpr : emptyIfNull(n.getAnnotations())) {
                    annotationExpr.accept(this, arg);
                }
                BlockStmt body = n.getBlock();
                if (body != null) {
                    visit(body, arg);
                }
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(MethodDeclaration n, A arg) {
            HashSet<String> blockVariables = newHashSet();
            this.localVariables.addLast(blockVariables);
            try {
                for (Parameter parameter : emptyIfNull(n.getParameters())) {
                    blockVariables.add(parameter.getId().getName());
                }
                for (AnnotationExpr annotationExpr : emptyIfNull(n.getAnnotations())) {
                    annotationExpr.accept(this, arg);
                }
                BlockStmt body = n.getBody();
                if (body != null) {
                    visit(body, arg);
                }
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(CatchClause n, A arg) {
            MultiTypeParameter multiTypeParameter = n.getExcept();
            HashSet<String> blockVariables = newHashSet();
            this.localVariables.addLast(blockVariables);
            try {
                blockVariables.add(multiTypeParameter.getId().getName());
                for (AnnotationExpr annotationExpr : emptyIfNull(multiTypeParameter.getAnnotations())) {
                    annotationExpr.accept(this, arg);
                }
                BlockStmt body = n.getCatchBlock();
                if (body != null) {
                    visit(body, arg);
                }
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(BlockStmt n, A arg) {
            this.localVariables.addLast(Sets.<String>newHashSet());
            try {
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(ForeachStmt n, A arg) {
            HashSet<String> blockVariables = newHashSet();
            this.localVariables.addLast(blockVariables);
            try {
                for (VariableDeclarator variableDeclarator : emptyIfNull(n.getVariable().getVars())) {
                    blockVariables.add(variableDeclarator.getId().getName());
                }
                n.getIterable().accept(this, arg);
                n.getBody().accept(this, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(ForStmt n, A arg) {
            this.localVariables.addLast(Sets.<String>newHashSet());
            try {
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(TryStmt n, A arg) {
            HashSet<String> blockVariables = newHashSet();
            this.localVariables.addLast(blockVariables);
            try {
                for (VariableDeclarationExpr variableDeclarationExpr : emptyIfNull(n.getResources())) {
                    for (VariableDeclarator variableDeclarator : variableDeclarationExpr.getVars()) {
                        blockVariables.add(variableDeclarator.getId().getName());
                    }
                }
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(LambdaExpr n, A arg) {
            HashSet<String> blockVariables = newHashSet();
            this.localVariables.addLast(blockVariables);
            try {
                for (Parameter parameter : emptyIfNull(n.getParameters())) {
                    blockVariables.add(parameter.getId().getName());
                }
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public void visit(VariableDeclarationExpr n, A arg) {
            for (AnnotationExpr annotationExpr : emptyIfNull(n.getAnnotations())) {
                annotationExpr.accept(this, arg);
            }
            n.getType().accept(this, arg);
            Set<String> blockVariables = this.localVariables.getLast();
            for (VariableDeclarator variableDeclarator : n.getVars()) {
                Expression expr = variableDeclarator.getInit();
                if (expr != null) {
                    expr.accept(this, arg);
                }
                blockVariables.add(variableDeclarator.getId().getName());
            }
        }

        protected final boolean aLocalVariableExists(@Nonnull String name) {
            return contains(concat(this.localVariables), name);
        }

        private void addFieldVariables(@Nonnull TypeDeclaration typeDeclaration, @Nonnull Set<String> variables) {
            addFieldVariables(typeDeclaration.getMembers(), variables);
        }

        private void addFieldVariables(@Nullable Iterable<? extends BodyDeclaration> declarations, @Nonnull Set<String> variables) {
            for (FieldDeclaration fieldDeclaration : emptyIfNull(declarations).
                    filter(FieldDeclaration.class).filter(not(constants()))) {
                for (VariableDeclarator variableDeclarator : fieldDeclaration.getVariables()) {
                    variables.add(variableDeclarator.getId().getName());
                }
            }
        }

    }

    private static class NameExprQualifier extends Qualifier<NameExpr> {

        public NameExprQualifier(NameExpr nameExpr) {
            super(nameExpr);
        }

        public NameExprQualifier(NameExpr nameExpr, FieldAccessExprQualifier parent) {
            super(nameExpr, parent);
        }

        @Nullable
        @Override
        protected Qualifier getScopeQualifier(@Nonnull NameExpr reference) {
            return null;
        }

        @Nonnull
        @Override
        protected String getName(@Nonnull NameExpr reference) {
            return reference.getName();
        }

        @Nonnull
        @Override
        protected String getFullQualifier(@Nonnull NameExpr reference) {
            return reference.getName();
        }

        @Override
        public boolean allowsPartialResolving() {
            return true;
        }

        @Nonnull
        @Override
        public Optional<String> examineInheritedType(@Nonnull CtClass referencingClazz,
                                                     @Nonnull CtClass inheritedClazz) {
            for (CtField ctField : inheritedClazz.getDeclaredFields()) {
                if (ctField.getName().equals(getName()) && ctField.visibleFrom(referencingClazz)) {
                    if (isConstant(ctField)) {
                        return of(inheritedClazz.getName());
                    }
                    // we want no reference to be established, so we refer to ourselves
                    return of(referencingClazz.getName());
                }
            }
            return absent();
        }

    }

    private static class FieldAccessExprQualifier extends Qualifier<FieldAccessExpr> {

        public FieldAccessExprQualifier(FieldAccessExpr fieldAccessExpr) {
            super(fieldAccessExpr);
        }

        private FieldAccessExprQualifier(FieldAccessExpr fieldAccessExpr, FieldAccessExprQualifier parent) {
            super(fieldAccessExpr, parent);
        }

        @Nullable
        @Override
        protected Qualifier<?> getScopeQualifier(@Nonnull FieldAccessExpr reference) {
            Expression scope = reference.getScope();
            return NameExpr.class.isInstance(scope)
                    ? new NameExprQualifier(NameExpr.class.cast(scope), this)
                    : new FieldAccessExprQualifier(FieldAccessExpr.class.cast(scope), this);
        }

        @Nonnull
        @Override
        protected String getName(@Nonnull FieldAccessExpr reference) {
            return reference.getField();
        }

        @Nonnull
        @Override
        protected String getFullQualifier(@Nonnull FieldAccessExpr reference) {
            return ReferenceToConstantsAnalyzer.getFullQualifier(reference);
        }

        @Override
        public boolean allowsPartialResolving() {
            return true;
        }

    }

}
