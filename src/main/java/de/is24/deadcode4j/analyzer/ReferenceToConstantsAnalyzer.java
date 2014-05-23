package de.is24.deadcode4j.analyzer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor;
import de.is24.javaparser.FixedGenericVisitorAdapter;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.Node;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.Utils.emptyIfNull;
import static de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor.classPoolAccessorFor;
import static java.lang.Math.max;
import static java.util.Arrays.asList;

public class ReferenceToConstantsAnalyzer extends JavaFileAnalyzer {

    @Nonnull
    private static String getFirstElement(@Nonnull FieldAccessExpr fieldAccessExpr) {
        Expression scope = fieldAccessExpr.getScope();
        if (NameExpr.class.isInstance(scope)) {
            return NameExpr.class.cast(scope).getName();
        }
        if (FieldAccessExpr.class.isInstance(scope)) {
            return getFirstElement(FieldAccessExpr.class.cast(scope));
        }
        throw new RuntimeException("Should not have reached this point!");
    }

    @Nonnull
    private static NameExpr getFirstNode(@Nonnull FieldAccessExpr fieldAccessExpr) {
        Expression scope = fieldAccessExpr.getScope();
        if (NameExpr.class.isInstance(scope)) {
            return NameExpr.class.cast(scope);
        }
        if (FieldAccessExpr.class.isInstance(scope)) {
            return getFirstNode(FieldAccessExpr.class.cast(scope));
        }
        throw new RuntimeException("Should not have reached this point!");
    }

    @Override
    protected void analyzeCompilationUnit(@Nonnull AnalysisContext AnalysisContext, @Nonnull CompilationUnit compilationUnit) {
        compilationUnit.accept(new CompilationUnitVisitor(AnalysisContext), null);
    }

    private static class CompilationUnitVisitor extends LocalVariableRecordingVisitor<Analysis, Analysis> {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final ClassPoolAccessor classPoolAccessor;
        private final AnalysisContext analysisContext;

        public CompilationUnitVisitor(AnalysisContext AnalysisContext) {
            this.classPoolAccessor = classPoolAccessorFor(AnalysisContext);
            this.analysisContext = AnalysisContext;
        }

        @Override
        public Analysis visit(CompilationUnit n, Analysis arg) {
            Analysis rootAnalysis = new Analysis(n.getPackage(), n.getImports());
            super.visit(n, rootAnalysis);
            return null;
        }

        @Override
        public Analysis visit(AnnotationDeclaration n, Analysis arg) {
            Analysis nestedAnalysis = new Analysis(arg);
            super.visit(n, nestedAnalysis);
            resolveFieldReferences(nestedAnalysis);
            resolveNameReferences(nestedAnalysis);
            return null;
        }

        @Override
        public Analysis visit(ClassOrInterfaceDeclaration n, Analysis arg) {
            Analysis nestedAnalysis = new Analysis(arg);
            super.visit(n, nestedAnalysis);
            resolveFieldReferences(nestedAnalysis);
            resolveNameReferences(nestedAnalysis);
            return null;
        }

        @Override
        public Analysis visit(EnumDeclaration n, Analysis arg) {
            Analysis nestedAnalysis = new Analysis(arg);
            super.visit(n, nestedAnalysis);
            resolveFieldReferences(nestedAnalysis);
            resolveNameReferences(nestedAnalysis);
            return null;
        }

        @Override
        public Analysis visit(MarkerAnnotationExpr n, Analysis arg) {
            // performance
            return null;
        }

        @Override
        public Analysis visit(NormalAnnotationExpr n, Analysis arg) {
            // performance
            for (final MemberValuePair m : emptyIfNull(n.getPairs())) {
                m.accept(this, arg);
            }
            return null;
        }

        @Override
        public Analysis visit(SingleMemberAnnotationExpr n, Analysis arg) {
            // performance
            return n.getMemberValue().accept(this, arg);
        }

        @Override
        public Analysis visit(FieldAccessExpr n, Analysis analysis) {
            if (isTargetOfAnAssignment(n) || isScopeOfAMethodCall(n) || isScopeOfThisExpression(n)) {
                return null;
            }
            if (!isRegularFieldAccessExpr(n)) {
                return super.visit(n, analysis);
            }
            // FQ beats all
            // then local variables & fields
            // now imports
            // then package access
            // then asterisk imports
            // finally java.lang
            if (FieldAccessExpr.class.isInstance(n.getScope())) {
                FieldAccessExpr nestedFieldAccessExpr = FieldAccessExpr.class.cast(n.getScope());
                if (isFullyQualifiedReference(nestedFieldAccessExpr)) {
                    return null;
                }
                if (aLocalVariableExists(getFirstElement(nestedFieldAccessExpr))) {
                    return null;
                }
                analysis.addFieldReference(nestedFieldAccessExpr);
            } else if (NameExpr.class.isInstance(n.getScope())) {
                String typeName = NameExpr.class.cast(n.getScope()).getName();
                if (aLocalVariableExists(typeName))
                    return null;

                analysis.addFieldReference(n);
            }
            return null;
        }

        /**
         * This is not entirely correct: while we want to filter calls like
         * <code>org.slf4j.LoggerFactory.getLogger("foo")</code>, we want to analyze
         * <code>foo.bar.FOO.substring(1)</code>.
         */
        private boolean isScopeOfAMethodCall(Expression expression) {
            return MethodCallExpr.class.isInstance(expression.getParentNode())
                    && expression == MethodCallExpr.class.cast(expression.getParentNode()).getScope();
        }

        private boolean isRegularFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
            Expression scope = fieldAccessExpr.getScope();
            return NameExpr.class.isInstance(scope) ||
                    FieldAccessExpr.class.isInstance(scope)
                            && isRegularFieldAccessExpr(FieldAccessExpr.class.cast(scope));
        }

        private boolean isFullyQualifiedReference(FieldAccessExpr fieldAccessExpr) {
            Optional<String> resolvedClass = resolveClass(fieldAccessExpr.toString());
            if (resolvedClass.isPresent()) {
                analysisContext.addDependencies(getTypeName(fieldAccessExpr), resolvedClass.get());
                return true;
            }
            return FieldAccessExpr.class.isInstance(fieldAccessExpr.getScope())
                    && isFullyQualifiedReference(FieldAccessExpr.class.cast(fieldAccessExpr.getScope()));
        }

        private Optional<String> resolveClass(String qualifier) {
            return this.classPoolAccessor.resolveClass(qualifier);
        }

        @Override
        public Analysis visit(NameExpr n, Analysis analysis) {
            if (isTargetOfAnAssignment(n) || isScopeOfAMethodCall(n) || isScopeOfThisExpression(n)) {
                return null;
            }
            if (aLocalVariableExists(n.getName())) {
                return null;
            }
            analysis.addNameReference(n);
            return null;
        }

        private boolean isScopeOfThisExpression(Expression n) {
            return ThisExpr.class.isInstance(n.getParentNode());
        }

        private boolean isTargetOfAnAssignment(Expression n) {
            return AssignExpr.class.isInstance(n.getParentNode()) && n == AssignExpr.class.cast(n.getParentNode()).getTarget();
        }

        private void resolveFieldReferences(Analysis analysis) {
            for (FieldAccessExpr fieldAccessExpr : analysis.getFieldReferences()) {
                if (refersToInnerType(fieldAccessExpr)
                        || refersToImport(fieldAccessExpr, analysis)
                        || refersToPackageType(fieldAccessExpr, analysis)
                        || refersToAsteriskImport(fieldAccessExpr, analysis)
                        || refersToJavaLang(fieldAccessExpr)
                        || refersToDefaultPackage(fieldAccessExpr, analysis)) {
                    continue;
                }
                logger.debug("Could not resolve reference [{}] defined within [{}].", fieldAccessExpr.toString(), getTypeName(fieldAccessExpr));
            }
        }

        private boolean refersToInnerType(@Nonnull FieldAccessExpr fieldAccessExpr) {
            NameExpr firstQualifier = getFirstNode(fieldAccessExpr);
            Node loopNode = fieldAccessExpr;
            for (; ; ) {
                if (TypeDeclaration.class.isInstance(loopNode)) {
                    TypeDeclaration typeDeclaration = TypeDeclaration.class.cast(loopNode);
                    if (resolveInnerReference(firstQualifier, asList(typeDeclaration))) {
                        return true;
                    }
                    if (resolveInnerReference(firstQualifier, emptyIfNull(typeDeclaration.getMembers()))) {
                        return true;
                    }
                } else if (CompilationUnit.class.isInstance(loopNode)
                        && resolveInnerReference(firstQualifier, CompilationUnit.class.cast(loopNode).getTypes())) {
                    return true;
                }
                loopNode = loopNode.getParentNode();
                if (loopNode == null) {
                    break;
                }
            }

            return false;
        }

        private boolean resolveInnerReference(@Nonnull NameExpr firstQualifier,
                                              @Nonnull Iterable<? extends BodyDeclaration> bodyDeclarations) {
            for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
                if (!TypeDeclaration.class.isInstance(bodyDeclaration)) {
                    continue;
                }
                final TypeDeclaration typeDeclaration = TypeDeclaration.class.cast(bodyDeclaration);
                if (firstQualifier.getName().equals(typeDeclaration.getName())) {
                    String referencedClass = resolveReferencedType(firstQualifier, typeDeclaration);
                    analysisContext.addDependencies(getTypeName(firstQualifier), referencedClass);
                    return true;
                }
            }
            return false;
        }

        @Nonnull
        private String resolveReferencedType(@Nonnull NameExpr firstQualifier,
                                             @Nonnull TypeDeclaration typeDeclaration) {
            if (FieldAccessExpr.class.isInstance(firstQualifier.getParentNode())) {
                NameExpr nextQualifier = FieldAccessExpr.class.cast(firstQualifier.getParentNode()).getFieldExpr();
                for (BodyDeclaration bodyDeclaration : emptyIfNull(typeDeclaration.getMembers())) {
                    if (!TypeDeclaration.class.isInstance(bodyDeclaration)) {
                        continue;
                    }
                    TypeDeclaration nextTypeDeclaration = TypeDeclaration.class.cast(bodyDeclaration);
                    if (nextQualifier.getName().equals(nextTypeDeclaration.getName())) {
                        return resolveReferencedType(nextQualifier, nextTypeDeclaration);
                    }
                }
            }

            return getTypeName(typeDeclaration);
        }

        private boolean refersToImport(FieldAccessExpr fieldAccessExpr, Analysis analysis) {
            String firstElement = getFirstElement(fieldAccessExpr);
            String anImport = analysis.getImport(firstElement);
            if (anImport != null) {
                return
                        refersToClass(fieldAccessExpr, anImport.substring(0, max(0, anImport.lastIndexOf('.') + 1)));
            }
            anImport = analysis.getStaticImport(firstElement);
            return anImport != null &&
                    refersToClass(fieldAccessExpr, anImport + ".");
        }

        private boolean refersToPackageType(FieldAccessExpr fieldAccessExpr, Analysis analysis) {
            String packagePrefix = analysis.packageName != null ? analysis.packageName + "." : "";
            return refersToClass(fieldAccessExpr, packagePrefix);
        }

        private boolean refersToAsteriskImport(FieldAccessExpr fieldAccessExpr, Analysis analysis) {
            for (String asteriskImport : analysis.getAsteriskImports()) {
                if (refersToClass(fieldAccessExpr, asteriskImport + "."))
                    return true;
            }
            return false;
        }

        private boolean refersToJavaLang(FieldAccessExpr fieldAccessExpr) {
            return refersToClass(fieldAccessExpr, "java.lang.");
        }

        private boolean refersToDefaultPackage(FieldAccessExpr fieldAccessExpr, Analysis analysis) {
            return analysis.packageName != null && refersToClass(fieldAccessExpr, "");
        }

        private boolean refersToClass(@Nonnull FieldAccessExpr fieldAccessExpr, @Nonnull String qualifierPrefix) {
            Optional<String> resolvedClass = resolveClass(qualifierPrefix + fieldAccessExpr.toString());
            if (resolvedClass.isPresent()) {
                analysisContext.addDependencies(getTypeName(fieldAccessExpr), resolvedClass.get());
                return true;
            }
            if (FieldAccessExpr.class.isInstance(fieldAccessExpr.getScope())) {
                return refersToClass(FieldAccessExpr.class.cast(fieldAccessExpr.getScope()), qualifierPrefix);
            }

            resolvedClass = resolveClass(qualifierPrefix + NameExpr.class.cast(fieldAccessExpr.getScope()).getName());
            if (resolvedClass.isPresent()) {
                analysisContext.addDependencies(getTypeName(fieldAccessExpr), resolvedClass.get());
                return true;
            }
            return false;
        }

        private void resolveNameReferences(Analysis analysis) {
            for (NameExpr reference : analysis.getNameReferences()) {
                final String referenceName = getTypeName(reference);
                String staticImport = analysis.getStaticImport(reference.getName());
                if (staticImport != null) {
                    // TODO this should probably be resolved
                    analysisContext.addDependencies(referenceName, staticImport);
                    continue;
                }
                // TODO handle asterisk static imports
                logger.debug("Could not resolve name reference [{}] defined within [{}].", reference, referenceName);
            }
        }

    }

    private static class LocalVariableRecordingVisitor<R, A> extends FixedGenericVisitorAdapter<R, A> {

        @Nonnull
        private final Deque<Set<String>> localVariables = newLinkedList();

        @Override
        public R visit(ClassOrInterfaceDeclaration n, A arg) {
            HashSet<String> fields = newHashSet();
            this.localVariables.addLast(fields);
            try {
                addFieldVariables(n, fields);
                return super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public R visit(EnumDeclaration n, A arg) {
            HashSet<String> fieldsAndEnums = newHashSet();
            this.localVariables.addLast(fieldsAndEnums);
            try {
                for (EnumConstantDeclaration enumConstantDeclaration : emptyIfNull(n.getEntries())) {
                    fieldsAndEnums.add(enumConstantDeclaration.getName());
                }
                addFieldVariables(n, fieldsAndEnums);
                return super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public R visit(ObjectCreationExpr n, A arg) {
            HashSet<String> fields = newHashSet();
            this.localVariables.addLast(fields);
            try {
                addFieldVariables(n.getAnonymousClassBody(), fields);
                return super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public R visit(ConstructorDeclaration n, A arg) {
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
            return null;
        }

        @Override
        public R visit(MethodDeclaration n, A arg) {
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
            return null;
        }

        @Override
        public R visit(CatchClause n, A arg) {
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
            return null;
        }

        @Override
        public R visit(BlockStmt n, A arg) {
            this.localVariables.addLast(Sets.<String>newHashSet());
            try {
                return super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public R visit(ForeachStmt n, A arg) {
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
            return null;
        }

        @Override
        public R visit(ForStmt n, A arg) {
            this.localVariables.addLast(Sets.<String>newHashSet());
            try {
                return super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
        }

        @Override
        public R visit(VariableDeclarationExpr n, A arg) {
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
            return null;
        }

        protected final boolean aLocalVariableExists(@Nonnull String name) {
            return contains(concat(this.localVariables), name);
        }

        private void addFieldVariables(@Nonnull TypeDeclaration typeDeclaration, @Nonnull Set<String> variables) {
            addFieldVariables(typeDeclaration.getMembers(), variables);
        }

        private void addFieldVariables(@Nullable Iterable<? extends BodyDeclaration> declarations, @Nonnull Set<String> variables) {
            for (BodyDeclaration bodyDeclaration : emptyIfNull(declarations)) {
                if (FieldDeclaration.class.isInstance(bodyDeclaration)) {
                    for (VariableDeclarator variableDeclarator : FieldDeclaration.class.cast(bodyDeclaration).getVariables()) {
                        variables.add(variableDeclarator.getId().getName());
                    }
                }
            }
        }

    }

    private static class Analysis {

        public final String packageName;
        private final List<ImportDeclaration> imports;
        private final List<FieldAccessExpr> fieldReferences = newArrayList();
        private final List<NameExpr> nameReferences = newArrayList();

        public Analysis(Analysis arg) {
            this.packageName = arg.packageName;
            this.imports = arg.imports;
        }

        public Analysis(PackageDeclaration packageName, List<ImportDeclaration> imports) {
            this.packageName = packageName == null ? null : packageName.getName().toString();
            this.imports = imports != null ? imports : Collections.<ImportDeclaration>emptyList();
        }

        private static Predicate<? super ImportDeclaration> isAsterisk() {
            return new Predicate<ImportDeclaration>() {
                @Override
                public boolean apply(@Nullable ImportDeclaration input) {
                    return input != null && input.isAsterisk();
                }
            };
        }

        private static Predicate<? super ImportDeclaration> isStatic() {
            return new Predicate<ImportDeclaration>() {
                @Override
                public boolean apply(@Nullable ImportDeclaration input) {
                    return input != null && input.isStatic();
                }
            };
        }

        private static Predicate<? super ImportDeclaration> refersTo(final String name) {
            return new Predicate<ImportDeclaration>() {
                @Override
                public boolean apply(@Nullable ImportDeclaration input) {
                    return input != null && input.getName().getName().equals(name);
                }
            };
        }

        private static Function<? super ImportDeclaration, ? extends String> toImportedType() {
            return new Function<ImportDeclaration, String>() {
                @Nullable
                @Override
                public String apply(@Nullable ImportDeclaration input) {
                    if (input == null)
                        return null;
                    NameExpr name = input.getName();
                    if (input.isStatic()) {
                        name = QualifiedNameExpr.class.cast(name).getQualifier();
                    }
                    return name.toString();
                }
            };
        }

        public void addFieldReference(FieldAccessExpr fieldAccessExpression) {
            this.fieldReferences.add(fieldAccessExpression);
        }

        public void addNameReference(NameExpr namedReference) {
            this.nameReferences.add(namedReference);
        }

        public Iterable<FieldAccessExpr> getFieldReferences() {
            return this.fieldReferences;
        }

        public Iterable<NameExpr> getNameReferences() {
            return this.nameReferences;
        }

        @SuppressWarnings("unchecked")
        public String getImport(String typeName) {
            return getOnlyElement(transform(filter(this.imports, and(refersTo(typeName), not(isAsterisk()), not(isStatic()))), toImportedType()), null);
        }

        @SuppressWarnings("unchecked")
        public String getStaticImport(String referenceName) {
            return getOnlyElement(transform(filter(this.imports, and(refersTo(referenceName), not(isAsterisk()), isStatic())), toImportedType()), null);
        }

        public Iterable<String> getAsteriskImports() {
            return transform(filter(this.imports, and(isAsterisk(), not(isStatic()))), toImportedType());
        }

    }

}
