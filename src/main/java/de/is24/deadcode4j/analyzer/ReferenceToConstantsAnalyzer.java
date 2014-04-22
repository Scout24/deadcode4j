package de.is24.deadcode4j.analyzer;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import de.is24.deadcode4j.CodeContext;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.visitor.GenericVisitorAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.is24.deadcode4j.analyzer.ReferenceToConstantsAnalyzer.Reference.referenceTo;
import static java.lang.Math.max;
import static java.util.Map.Entry;

// TODO visit everything, resolve references later on
public class ReferenceToConstantsAnalyzer extends AnalyzerAdapter {

    private final Collection<Analysis> resultsNeedingPostProcessing = newArrayList();

    @Nullable
    private static String getFirstElement(@Nonnull FieldAccessExpr fieldAccessExpr) {
        Expression scope = fieldAccessExpr.getScope();
        if (NameExpr.class.isInstance(scope)) {
            return NameExpr.class.cast(scope).getName();
        }
        if (FieldAccessExpr.class.isInstance(scope)) {
            return getFirstElement(FieldAccessExpr.class.cast(scope));
        }
        return null;
    }

    @Override
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull File file) {
        if (file.getName().endsWith(".java")) {
            logger.debug("Analyzing Java file [{}]...", file);
            analyzeJavaFile(codeContext, file);
        }
    }

    @Override
    public void finishAnalysis(@Nonnull CodeContext codeContext) {
        Collection<String> analyzedClasses = codeContext.getAnalyzedCode().getAnalyzedClasses();
        for (Analysis analysis : resultsNeedingPostProcessing) {
            for (Entry<String, String> referenceToPackageType : analysis.referencesToPackageType.entrySet()) {
                String depender = referenceToPackageType.getKey();
                String dependee = referenceToPackageType.getValue();
                // package wins over asterisk import
                if (analyzedClasses.contains(dependee)) {
                    codeContext.addDependencies(depender, dependee);
                } else {
                    String className = dependee.substring(dependee.lastIndexOf('.'));
                    for (String asteriskImport : analysis.getAsteriskImports()) {
                        dependee = asteriskImport + className;
                        if (analyzedClasses.contains(dependee)) {
                            codeContext.addDependencies(depender, dependee);
                        }
                    }
                }
            }
            withNextFieldAccess:
            for (Entry<FieldAccessExpr, String> fieldAccess : analysis.fullyQualifiedOrPackageAccesses.entrySet()) {
                String depender = fieldAccess.getValue();
                String dependee = fieldAccess.getKey().toString();

                String rootName = getFirstElement(fieldAccess.getKey());
                String className = analysis.packageName + "." + rootName;
                if (analyzedClasses.contains(className)) {
                    codeContext.addDependencies(depender, analysis.packageName + "." + dependee);
                    //noinspection UnnecessaryLabelOnContinueStatement
                    continue withNextFieldAccess;
                }
                for (String asteriskImport : analysis.getAsteriskImports()) {
                    className = asteriskImport + "." + rootName;
                    if (analyzedClasses.contains(className)) {
                        codeContext.addDependencies(depender, asteriskImport + "." + dependee);
                        continue withNextFieldAccess;
                    }
                }
                codeContext.addDependencies(depender, dependee);
            }
        }
    }

    private void analyzeJavaFile(final CodeContext codeContext, File file) {
        CompilationUnit compilationUnit;
        try {
            compilationUnit = JavaParser.parse(file, null, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse [" + file + "]!", e);
        }

        Analysis result = compilationUnit.accept(new CompilationUnitVisitor(codeContext), null);
        if (result.needsPostProcessing()) {
            this.resultsNeedingPostProcessing.add(result);
        }
    }

    private static class CompilationUnitVisitor extends GenericVisitorAdapter<Analysis, Analysis> {

        private final CodeContext codeContext;
        private final Deque<Set<String>> localVariables = newLinkedList();
        /**
         * @deprecated should go away
         */
        @Deprecated
        private final Set<String> innerTypes = newHashSet();
        private final Map<String, String> referenceToInnerOrPackageType = newHashMap();
        private final Map<FieldAccessExpr, String> fieldAccesses = newHashMap();
        /**
         * @deprecated should go away
         */
        @Deprecated
        private String typeName;
        private List<Reference> nameReferences = newArrayList();

        public CompilationUnitVisitor(CodeContext codeContext) {
            this.codeContext = codeContext;
        }

        @Override
        public Analysis visit(CompilationUnit n, Analysis arg) {
            Analysis rootAnalysis = new Analysis(n.getPackage(), n.getImports());
            super.visit(n, rootAnalysis);
            resolveInnerTypeReferences(rootAnalysis);
            Map<FieldAccessExpr, String> fullyQualifiedOrPackageAccesses = resolveFieldAccesses(rootAnalysis);
            return new Analysis(rootAnalysis, this.referenceToInnerOrPackageType, fullyQualifiedOrPackageAccesses);
        }

        @Override
        public Analysis visit(AnnotationDeclaration n, Analysis arg) {
            String name = n.getName();
            registerType(name);
            Analysis nestedAnalysis = new Analysis(arg, name);
            super.visit(n, nestedAnalysis);
            resolveNameReferences(nestedAnalysis);
            return null;
        }

        @Override
        public Analysis visit(ClassOrInterfaceDeclaration n, Analysis arg) {
            String name = n.getName();
            registerType(name);
            Analysis nestedAnalysis = new Analysis(arg, name);
            super.visit(n, nestedAnalysis);
            resolveNameReferences(nestedAnalysis);
            return null;
        }

        @Override
        public Analysis visit(EnumDeclaration n, Analysis arg) {
            String name = n.getName();
            registerType(name);
            Analysis nestedAnalysis = new Analysis(arg, name);
            super.visit(n, nestedAnalysis);
            resolveNameReferences(nestedAnalysis);
            return null;
        }

        @Override
        public Analysis visit(FieldDeclaration n, Analysis arg) {
            for (VariableDeclarator variableDeclarator : n.getVariables()) {
                arg.addFieldName(variableDeclarator.getId().getName());
            }
            super.visit(n, arg);
            return null;
        }

        @Override
        public Analysis visit(BlockStmt n, Analysis arg) {
            this.localVariables.addLast(Sets.<String>newHashSet());
            try {
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
            return null;
        }

        @Override
        public Analysis visit(ForeachStmt n, Analysis arg) {
            HashSet<String> blockVariables = newHashSet();
            this.localVariables.addLast(blockVariables);
            try {
                for (VariableDeclarator variableDeclarator : n.getVariable().getVars()) {
                    blockVariables.add(variableDeclarator.getId().getName());
                }
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
            return null;
        }

        @Override
        public Analysis visit(ForStmt n, Analysis arg) {
            this.localVariables.addLast(Sets.<String>newHashSet());
            try {
                super.visit(n, arg);
            } finally {
                this.localVariables.removeLast();
            }
            return null;
        }

        @Override
        public Analysis visit(VariableDeclarationExpr n, Analysis arg) {
            Set<String> blockVariables = this.localVariables.getLast();
            for (VariableDeclarator variableDeclarator : n.getVars()) {
                blockVariables.add(variableDeclarator.getId().getName());
            }
            super.visit(n, arg);
            return null;
        }

        @Override
        public Analysis visit(FieldAccessExpr n, Analysis arg) {
            if (MethodCallExpr.class.isInstance(n.getParentNode())) {
                if (n == MethodCallExpr.class.cast(n.getParentNode()).getScope())
                    return null;
            }
            if (FieldAccessExpr.class.isInstance(n.getScope())) {
                this.fieldAccesses.put(FieldAccessExpr.class.cast(n.getScope()), arg.getTypeName());
            } else if (NameExpr.class.isInstance(n.getScope())) {
                String typeName = NameExpr.class.cast(n.getScope()).getName();
                if (!arg.isFieldDefined(typeName) && !aLocalVariableExists(typeName)) {
                    String referencedType = arg.getImport(typeName);
                    if (referencedType != null) {
                        codeContext.addDependencies(arg.getTypeName(), referencedType);
                        return null;
                    }
                    referencedType = arg.getStaticImport(typeName);
                    if (referencedType != null) {
                        codeContext.addDependencies(arg.getTypeName(), referencedType + "." + typeName);
                        return null;
                    }
                    this.referenceToInnerOrPackageType.put(arg.getTypeName(), typeName);
                }
            }
            return null;
        }

        @Override
        public Analysis visit(NameExpr n, Analysis arg) {
            if (SwitchEntryStmt.class.isInstance(n.getParentNode())) {
                return null;
            }
            if (AssignExpr.class.isInstance(n.getParentNode()) && n == AssignExpr.class.cast(n.getParentNode()).getTarget()) {
                return null;
            }
            String namedReference = n.getName();
            if (aLocalVariableExists(namedReference)) {
                return null;
            }
            this.nameReferences.add(referenceTo(namedReference).by(arg.getTypeName()));
            return null;
        }

        private boolean aLocalVariableExists(String name) {
            return contains(concat(this.localVariables), name);
        }

        private void resolveNameReferences(Analysis analysis) {
            for (Reference reference : this.nameReferences) {
                String referenceName = reference.to;
                if (analysis.isFieldDefined(referenceName)) {
                    continue;
                }
//                if (this.innerTypes.contains(referenceName)) {
//                    codeContext.addDependencies(reference.by,
//                            this.packageName + "." + this.typeName + "$" + referenceName);
//                    continue;
//                }
                String staticImport = analysis.getStaticImport(referenceName);
                if (staticImport != null) {
                    codeContext.addDependencies(reference.by, staticImport);
                }
                // TODO handle asterisk static imports
            }
        }

        @SuppressWarnings("deprecation")
        private void resolveInnerTypeReferences(Analysis analysis) {
            Iterator<Entry<String, String>> namedReferences = this.referenceToInnerOrPackageType.entrySet().iterator();
            while (namedReferences.hasNext()) {
                Entry<String, String> referenceToInnerOrPackageType = namedReferences.next();
                String namedReference = referenceToInnerOrPackageType.getValue();
                if (this.innerTypes.contains(namedReference)) {
                    codeContext.addDependencies(referenceToInnerOrPackageType.getKey(),
                            analysis.packageName + "." + this.typeName + "$" + namedReference);
                    namedReferences.remove();
                }
            }
        }

        @SuppressWarnings("deprecation")
        private Map<FieldAccessExpr, String> resolveFieldAccesses(Analysis analysis) {
            Map<FieldAccessExpr, String> fullyQualifiedOrPackageAccesses = newHashMap();
            for (Entry<FieldAccessExpr, String> fieldAccess : fieldAccesses.entrySet()) {
                String rootName = getFirstElement(fieldAccess.getKey());
                if (rootName == null) {
                    continue;
                }
                if (this.innerTypes.contains(rootName)) {
                    codeContext.addDependencies(fieldAccess.getValue(),
                            analysis.packageName + "." + this.typeName + "$" + fieldAccess.getKey().toString());
                    continue;
                }
                String referencedType = analysis.getImport(rootName);
                if (referencedType != null) {
                    codeContext.addDependencies(fieldAccess.getValue(),
                            referencedType.substring(0, max(0, referencedType.lastIndexOf('.') + 1)) + fieldAccess.getKey().toString());
                    continue;
                }
                fullyQualifiedOrPackageAccesses.put(fieldAccess.getKey(), fieldAccess.getValue());
            }
            return fullyQualifiedOrPackageAccesses;
        }

        @SuppressWarnings("deprecation")
        private void registerType(String typeName) {
            if (this.typeName == null) {
                this.typeName = typeName;
            } else {
                this.innerTypes.add(typeName);
            }
        }

    }

    private static class Analysis {

        public final String packageName;
        private final List<ImportDeclaration> imports;
        private final Analysis parent;
        private final String typeName;
        private final Set<String> fieldNames = newHashSet();
        public Map<FieldAccessExpr, String> fullyQualifiedOrPackageAccesses;
        public Map<String, String> referencesToPackageType;

        @SuppressWarnings("deprecation")
        @Deprecated
        public Analysis(Analysis parent, Map<String, String> referencesToNamedType, Map<FieldAccessExpr, String> fullyQualifiedOrPackageAccesses) {
            this.parent = null;
            this.typeName = null;

            this.imports = parent.imports;
            this.packageName = parent.packageName;
            this.fullyQualifiedOrPackageAccesses = fullyQualifiedOrPackageAccesses;
            this.referencesToPackageType = newHashMap();
            for (Entry<String, String> referenceToPackageType : referencesToNamedType.entrySet()) {
                this.referencesToPackageType.put(referenceToPackageType.getKey(),
                        packageName + "." + referenceToPackageType.getValue());
            }
        }

        public Analysis(Analysis arg, String typeName) {
            this.parent = arg;
            this.typeName = typeName;
            this.packageName = arg.packageName;
            this.imports = arg.imports;
        }

        public Analysis(PackageDeclaration packageName, List<ImportDeclaration> imports) {
            this.packageName = packageName == null ? null : packageName.getName().toString();
            this.imports = imports != null ? imports : Collections.<ImportDeclaration>emptyList();
            this.parent = null;
            this.typeName = null;
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

        public boolean needsPostProcessing() {
            return !(this.referencesToPackageType.isEmpty() && this.fullyQualifiedOrPackageAccesses.isEmpty());
        }

        public void addFieldName(String name) {
            this.fieldNames.add(name);
        }

        public boolean isFieldDefined(String referenceName) {
            return this.fieldNames.contains(referenceName) || hasParent() && this.parent.isFieldDefined(referenceName);
        }

        private boolean hasParent() {
            return this.parent != null;
        }

        public String getTypeName() {
            boolean isNested = false;
            StringBuilder buffy = new StringBuilder();
            if (hasParent()) {
                String parentTypeName = parent.getTypeName();
                if (parentTypeName != null) {
                    buffy.append(parentTypeName);
                    isNested = true;
                }
            }
            if (this.typeName != null) {
                if (isNested) {
                    buffy.append("$");
                } else {
                    buffy.append(this.packageName).append('.');
                }
                buffy.append(this.typeName);
            }

            return buffy.length() > 0 ? buffy.toString() : null;
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

    static class Reference {

        public final String to;
        public final String by;

        private Reference(String to, String by) {
            this.to = to;
            this.by = by;
        }

        public static ReferenceBuilder referenceTo(String name) {
            return new ReferenceBuilder(name);
        }

        private static class ReferenceBuilder {
            private final String name;

            public ReferenceBuilder(String name) {
                this.name = name;
            }

            public Reference by(String referrer) {
                return new Reference(this.name, referrer);
            }
        }

    }

}
