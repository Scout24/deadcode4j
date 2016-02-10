package de.is24.deadcode4j.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.TokenMgrError;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.deadcode4j.analyzer.javassist.ClassPoolAccessor;
import de.is24.guava.NonNullFunction;
import de.is24.guava.SequentialLoadingCache;
import de.is24.javaparser.Nodes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayList;
import static de.is24.deadcode4j.Utils.emptyIfNull;
import static de.is24.guava.NonNullFunctions.or;
import static de.is24.guava.NonNullFunctions.toFunction;
import static de.is24.javaparser.ImportDeclarations.isAsterisk;
import static de.is24.javaparser.ImportDeclarations.refersTo;
import static de.is24.javaparser.Nodes.getTypeName;
import static de.is24.javaparser.Nodes.prepend;
import static de.is24.javassist.CtClasses.*;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Serves as a base class with which to analyze java files.
 *
 * @since 2.0.0
 */
public abstract class JavaFileAnalyzer extends AnalyzerAdapter {

    private static final String JAVA_PARSER_KEY = JavaFileAnalyzer.class.getName() + ":JavaParser";
    private static final NonNullFunction<AnalysisContext, LoadingCache<File, Optional<CompilationUnit>>>
            JAVA_PARSER_SUPPLIER = new JavaParserSupplier(true);

    private static final String TYPE_RESOLVER_KEY = JavaFileAnalyzer.class.getName() + ":TypeResolver";
    private static final NonNullFunction<AnalysisContext, NonNullFunction<Qualifier<?>, Optional<String>>>
            TYPE_RESOLVER_SUPPLIER =
            new NonNullFunction<AnalysisContext, NonNullFunction<Qualifier<?>, Optional<String>>>() {
                @Nonnull
                @Override
                public NonNullFunction<Qualifier<?>, Optional<String>> apply(@Nonnull AnalysisContext analysisContext) {
                    final ClassPoolAccessor classPoolAccessor = ClassPoolAccessor.classPoolAccessorFor(analysisContext);
                    return new NonNullFunction<Qualifier<?>, Optional<String>>() {
                        @Nonnull
                        @Override
                        @SuppressWarnings("unchecked")
                        public Optional<String> apply(@Nonnull Qualifier<?> input) {
                            return or(
                                    new FullyQualifiedTypeResolver(classPoolAccessor),
                                    new InnerTypeResolver(),
                                    new InheritedTypeResolver(classPoolAccessor),
                                    new ImportedTypeResolver(classPoolAccessor),
                                    new PackageTypeResolver(classPoolAccessor),
                                    new AsteriskImportedTypeResolver(classPoolAccessor),
                                    new JavaLangTypeResolver(classPoolAccessor)
                            ).apply(input);
                        }
                    };
                }
            };

    private static LoadingCache<File, Optional<CompilationUnit>> getJavaFileParser(AnalysisContext analysisContext) {
        return analysisContext.getOrCreateCacheEntry(JAVA_PARSER_KEY, JAVA_PARSER_SUPPLIER);
    }

    private static NonNullFunction<Qualifier<?>, Optional<String>> getTypeResolver(AnalysisContext analysisContext) {
        return analysisContext.getOrCreateCacheEntry(TYPE_RESOLVER_KEY, TYPE_RESOLVER_SUPPLIER);
    }

    /**
     * Resolves a type reference by means of the given {@code Qualifier}.
     *
     * @see de.is24.deadcode4j.analyzer.JavaFileAnalyzer.Qualifier
     * @since 2.0.0
     */
    @Nonnull
    protected static Optional<String> resolveType(@Nonnull final AnalysisContext analysisContext, @Nonnull Qualifier qualifier) {
        Optional<String> resolvedClass = getTypeResolver(analysisContext).apply(qualifier);
        if (!qualifier.allowsPartialResolving()
                && resolvedClass.isPresent()
                && !isFullyResolved(resolvedClass.get(), qualifier)) {
            return absent();
        }
        return resolvedClass;
    }

    protected static boolean isFullyResolved(@Nonnull String resolvedClass, @Nonnull Qualifier qualifier) {
        return resolvedClass.replace('$', '.').endsWith(qualifier.getFullQualifier().replace('$', '.'));
    }

    @Override
    public final void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File file) {
        if (file.getName().endsWith(".java")) {
            Optional<CompilationUnit> compilationUnit = getJavaFileParser(analysisContext).getUnchecked(file);
            if (compilationUnit.isPresent()) {
                logger.debug("Analyzing Java file [{}]...", file);
                analyzeCompilationUnit(analysisContext, compilationUnit.get());
            }
        }
    }

    /**
     * Perform an analysis for the specified java file.
     * Results must be reported via the capabilities of the {@link AnalysisContext}.
     *
     * @since 2.0.0
     */
    protected abstract void analyzeCompilationUnit(@Nonnull AnalysisContext analysisContext, @Nonnull CompilationUnit compilationUnit);

    /**
     * Subclasses of {@code Qualifier} are used to resolve types by providing an environment to analyze.
     *
     * @see #resolveType(de.is24.deadcode4j.AnalysisContext, de.is24.deadcode4j.analyzer.JavaFileAnalyzer.Qualifier)
     * @since 2.0.0
     */
    protected static abstract class Qualifier<T extends Node> {

        @Nonnull
        private final T reference;
        @Nonnull
        private final String name;
        @Nonnull
        private final String fullQualifier;
        @Nullable
        private final Qualifier<?> parentQualifier;
        @Nullable
        private final Qualifier<?> scopeQualifier;

        protected Qualifier(@Nonnull T reference, @Nullable Qualifier<?> parent) {
            this.reference = reference;
            this.parentQualifier = parent;
            this.scopeQualifier = getScopeQualifier(reference);
            this.name = getName(reference);
            this.fullQualifier = getFullQualifier(reference);
        }

        protected Qualifier(@Nonnull T reference) {
            this(reference, null);
        }

        /**
         * Must return the name of the level/scope this qualifier represents.
         *
         * @since 2.0.0
         */
        @Nonnull
        protected abstract String getName(@Nonnull T reference);

        /**
         * Must return the full qualifier name of this level/scope and below.
         *
         * @since 2.0.0
         */
        @Nonnull
        protected abstract String getFullQualifier(@Nonnull T reference);

        /**
         * Must return the qualifier of the level/scope below.
         *
         * @since 2.0.0
         */
        @Nullable
        protected abstract Qualifier<?> getScopeQualifier(@Nonnull T reference);

        /**
         * Indicates if this qualifier can be resolved partially or must be resolved completely.
         *
         * @since 2.0.0
         */
        protected abstract boolean allowsPartialResolving();

        @Nonnull
        protected final T getNode() {
            return this.reference;
        }

        @Nonnull
        protected final String getName() {
            return this.name;
        }

        @Nonnull
        protected final String getFullQualifier() {
            return fullQualifier;
        }

        @Nullable
        protected final Qualifier<?> getParentQualifier() {
            return this.parentQualifier;
        }

        @Nullable
        protected final Qualifier<?> getScopeQualifier() {
            return this.scopeQualifier;
        }

        @Nonnull
        protected final Qualifier<?> getFirstQualifier() {
            for (Qualifier<?> currentScope = this, nextScope; ; ) {
                nextScope = currentScope.getScopeQualifier();
                if (nextScope == null) {
                    return currentScope;
                }
                currentScope = nextScope;
            }
        }

        protected final boolean isSingleQualifier() {
            return this == getFirstQualifier();
        }

        @Nonnull
        protected final Iterable<? extends Qualifier> getTypeCandidates() {
            if (!allowsPartialResolving()) {
                return Collections.<Qualifier<? extends Node>>singleton(this);
            }
            List<Qualifier<?>> candidates = newArrayList();
            for (Qualifier<?> loopQualifier = this; ; ) {
                candidates.add(loopQualifier);
                loopQualifier = loopQualifier.getScopeQualifier();
                if (loopQualifier == null) {
                    return candidates;
                }
            }
        }

        /**
         * This hook allows to further analyze an inherited type.
         *
         * @return the name of the class this qualifier refers to
         * @since 2.0.0
         */
        @Nonnull
        protected Optional<String> examineInheritedType(@Nonnull CtClass referencingClazz,
                                                        @Nonnull CtClass inheritedClazz) {
            return absent();
        }

    }

    private static abstract class RequiresClassPoolAccessor {
        @Nonnull
        protected final ClassPoolAccessor classPoolAccessor;

        protected RequiresClassPoolAccessor(@Nonnull ClassPoolAccessor classPoolAccessor) {
            this.classPoolAccessor = classPoolAccessor;
        }
    }

    private static abstract class CandidatesResolver extends RequiresClassPoolAccessor
            implements NonNullFunction<Qualifier<?>, Optional<String>> {

        protected CandidatesResolver(@Nonnull ClassPoolAccessor classPoolAccessor) {
            super(classPoolAccessor);
        }

        @Nonnull
        protected Iterable<String> calculatePrefixes(@Nonnull Qualifier<?> topQualifier) {
            String prefix = calculatePrefix(topQualifier);
            return prefix != null ? singletonList(prefix) : Collections.<String>emptyList();
        }

        @Nullable
        protected String calculatePrefix(@Nonnull Qualifier<?> topQualifier) {
            return null;
        }

        protected boolean skipResolvingFor(@Nonnull Qualifier<?> candidate) {
            return false;
        }

        @Nonnull
        @Override
        public final Optional<String> apply(@Nonnull Qualifier<?> input) {
            for (CharSequence prefix : calculatePrefixes(input)) {
                for (Qualifier candidate : input.getTypeCandidates()) {
                    if (skipResolvingFor(candidate)) {
                        continue;
                    }
                    Optional<String> resolvedClass = classPoolAccessor.resolveClass(prefix + candidate.getFullQualifier());
                    if (resolvedClass.isPresent()) {
                        return resolvedClass;
                    }
                }
            }
            return absent();
        }

    }

    private static class FullyQualifiedTypeResolver extends CandidatesResolver {

        public FullyQualifiedTypeResolver(ClassPoolAccessor classPoolAccessor) {
            super(classPoolAccessor);
        }

        @Nonnull
        @Override
        protected String calculatePrefix(@Nonnull Qualifier<?> topQualifier) {
            return "";
        }

        @Override
        protected boolean skipResolvingFor(@Nonnull Qualifier<?> candidate) {
            return candidate.isSingleQualifier();
        }

    }

    private static class InnerTypeResolver implements NonNullFunction<Qualifier<?>, Optional<String>> {

        @Nonnull
        @Override
        public Optional<String> apply(@Nonnull Qualifier<?> typeReference) {
            Qualifier firstQualifier = typeReference.getFirstQualifier();
            for (Node loopNode = typeReference.getNode(); ; ) {
                Optional<String> reference;
                if (TypeDeclaration.class.isInstance(loopNode)) {
                    TypeDeclaration typeDeclaration = TypeDeclaration.class.cast(loopNode);
                    reference = resolveInnerReference(firstQualifier, singleton(typeDeclaration));
                    if (reference.isPresent()) {
                        return reference;
                    }
                    reference = resolveInnerReference(firstQualifier, typeDeclaration.getMembers());
                    if (reference.isPresent()) {
                        return reference;
                    }
                } else if (CompilationUnit.class.isInstance(loopNode)) {
                    reference = resolveInnerReference(firstQualifier, CompilationUnit.class.cast(loopNode).getTypes());
                    if (reference.isPresent()) {
                        return reference;
                    }
                }
                loopNode = loopNode.getParentNode();
                if (loopNode == null) {
                    return absent();
                }
            }
        }

        @Nonnull
        private Optional<String> resolveInnerReference(
                @Nonnull Qualifier firstQualifier,
                @Nullable Iterable<? extends BodyDeclaration> bodyDeclarations) {
            for (TypeDeclaration typeDeclaration : emptyIfNull(bodyDeclarations).filter(TypeDeclaration.class)) {
                if (firstQualifier.getName().equals(typeDeclaration.getName())) {
                    return of(resolveReferencedType(firstQualifier, typeDeclaration));
                }
            }
            return absent();
        }

        @Nonnull
        private String resolveReferencedType(@Nonnull Qualifier qualifier, @Nonnull TypeDeclaration type) {
            Qualifier parentQualifier = qualifier.getParentQualifier();
            if (parentQualifier != null) {
                for (TypeDeclaration innerType : emptyIfNull(type.getMembers()).filter(TypeDeclaration.class)) {
                    if (parentQualifier.getName().equals(innerType.getName())) {
                        return resolveReferencedType(parentQualifier, innerType);
                    }
                }
            }

            return getTypeName(type);
        }

    }

    private static class InheritedTypeResolver extends RequiresClassPoolAccessor
            implements NonNullFunction<Qualifier<?>, Optional<String>> {

        public InheritedTypeResolver(@Nonnull ClassPoolAccessor classPoolAccessor) {
            super(classPoolAccessor);
        }

        @Nonnull
        @Override
        public Optional<String> apply(@Nonnull Qualifier<?> typeReference) {
            String typeName = getTypeName(typeReference.getNode());
            CtClass clazz = getCtClass(classPoolAccessor.getClassPool(), typeName);
            if (clazz == null) {
                return absent();
            }
            Qualifier firstQualifier = typeReference.getFirstQualifier();
            for (CtClass declaringClazz : getDeclaringClassesOf(clazz)) {
                Optional<String> inheritedType = resolveInheritedType(clazz, declaringClazz, firstQualifier);
                if (inheritedType.isPresent()) {
                    return inheritedType;
                }
            }
            return absent();
        }

        @Nonnull
        private Optional<String> resolveInheritedType(@Nonnull CtClass referencingClazz,
                                                      @Nonnull CtClass clazz,
                                                      @Nonnull Qualifier firstQualifier) {
            @SuppressWarnings("unchecked")
            Optional<String> result = firstQualifier.examineInheritedType(referencingClazz, clazz);
            if (result.isPresent()) {
                return result;
            }
            result = checkNestedClasses(referencingClazz, getSuperclassOf(clazz), firstQualifier);
            if (result.isPresent()) {
                return result;
            }
            for (CtClass interfaceClazz : getInterfacesOf(clazz)) {
                result = checkNestedClasses(referencingClazz, interfaceClazz, firstQualifier);
                if (result.isPresent()) {
                    return result;
                }
            }
            return absent();
        }

        @Nonnull
        private Optional<String> checkNestedClasses(@Nonnull CtClass referencingClazz,
                                                    @Nullable CtClass clazz,
                                                    @Nonnull Qualifier firstQualifier) {
            if (clazz == null || isJavaLangObject(clazz)) {
                return absent();
            }
            for (CtClass nestedClass : getNestedClassesOf(clazz)) {
                if (nestedClass.getName().substring(clazz.getName().length() + 1).equals(firstQualifier.getName())) {
                    return resolveNestedType(firstQualifier, nestedClass);
                }
            }
            return resolveInheritedType(referencingClazz, clazz, firstQualifier);
        }

        private Optional<String> resolveNestedType(Qualifier qualifier, CtClass clazz) {
            Qualifier parentQualifier = qualifier.getParentQualifier();
            if (parentQualifier != null) {
                for (CtClass nestedClass : getNestedClassesOf(clazz)) {
                    if (nestedClass.getName().substring(clazz.getName().length() + 1)
                            .equals(parentQualifier.getName())) {
                        return resolveNestedType(parentQualifier, nestedClass);
                    }
                }
            }
            return of(clazz.getName());
        }

    }

    private static class ImportedTypeResolver extends CandidatesResolver {

        public ImportedTypeResolver(ClassPoolAccessor classPoolAccessor) {
            super(classPoolAccessor);
        }

        @Nullable
        @Override
        protected String calculatePrefix(@Nonnull Qualifier<?> topQualifier) {
            String firstQualifier = topQualifier.getFirstQualifier().getName();
            CompilationUnit compilationUnit = Nodes.getCompilationUnit(topQualifier.getNode());
            ImportDeclaration importDeclaration = getOnlyElement(emptyIfNull(compilationUnit.getImports()).filter(
                    and(not(isAsterisk()), refersTo(firstQualifier))), null);
            if (importDeclaration == null) {
                return null;
            }
            StringBuilder buffy = prepend(importDeclaration.getName(), new StringBuilder());
            int beginIndex = buffy.length() - firstQualifier.length();
            return beginIndex == 0 ? "" :
                    buffy.replace(beginIndex - 1, buffy.length(), importDeclaration.isStatic() ? "$" : ".").toString();
        }

    }

    private static class PackageTypeResolver extends CandidatesResolver {

        public PackageTypeResolver(@Nonnull ClassPoolAccessor classPoolAccessor) {
            super(classPoolAccessor);
        }

        @Nonnull
        @Override
        protected String calculatePrefix(@Nonnull Qualifier<?> topQualifier) {
            PackageDeclaration aPackage = Nodes.getCompilationUnit(topQualifier.getNode()).getPackage();
            if (aPackage == null) {
                return "";
            }
            return prepend(aPackage.getName(), new StringBuilder("")).append(".").toString();
        }

    }

    private static class AsteriskImportedTypeResolver extends CandidatesResolver {

        public AsteriskImportedTypeResolver(@Nonnull ClassPoolAccessor classPoolAccessor) {
            super(classPoolAccessor);
        }

        @Nonnull
        @Override
        protected Iterable<String> calculatePrefixes(@Nonnull Qualifier<?> topQualifier) {
            ArrayList<String> asteriskImports = newArrayList();
            CompilationUnit compilationUnit = Nodes.getCompilationUnit(topQualifier.getNode());
            for (ImportDeclaration importDeclaration :
                    emptyIfNull(compilationUnit.getImports()).filter(isAsterisk())) {
                StringBuilder buffy = prepend(importDeclaration.getName(), new StringBuilder());
                buffy.append(importDeclaration.isStatic() ? '$' : '.');
                asteriskImports.add(buffy.toString());
            }
            return asteriskImports;
        }

    }

    private static class JavaLangTypeResolver extends CandidatesResolver {

        public JavaLangTypeResolver(@Nonnull ClassPoolAccessor classPoolAccessor) {
            super(classPoolAccessor);
        }

        @Nonnull
        @Override
        protected String calculatePrefix(@Nonnull Qualifier<?> topQualifier) {
            return "java.lang.";
        }

    }

    private static class JavaParserSupplier implements NonNullFunction<AnalysisContext, LoadingCache<File, Optional<CompilationUnit>>> {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final boolean ignoreParsingErrors;

        JavaParserSupplier(boolean ignoreParsingErrors) {
            this.ignoreParsingErrors = ignoreParsingErrors;
        }

        @Nonnull
        @Override
        @SuppressWarnings("PMD.AvoidCatchingThrowable") // unfortunately, JavaParser throws an Error when parsing fails
        public LoadingCache<File, Optional<CompilationUnit>> apply(@Nonnull final AnalysisContext analysisContext) {
            return SequentialLoadingCache.createSingleValueCache(toFunction(new NonNullFunction<File, Optional<CompilationUnit>>() {
                @Nonnull
                @Override
                @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "The MavenProject does not provide the proper encoding")
                public Optional<CompilationUnit> apply(@Nonnull File file) {
                    Reader reader = null;
                    try {
                        reader = analysisContext.getModule().getEncoding() != null
                                ? new InputStreamReader(new FileInputStream(file),
                                analysisContext.getModule().getEncoding())
                                : new FileReader(file);
                        return of(JavaParser.parse(reader, false));
                    } catch (Throwable t) {
                        return handleThrowable(file, t);
                    } finally {
                        closeQuietly(reader);
                    }
                }
            }));
        }
        private Optional<CompilationUnit> handleThrowable(File file, Throwable t) {
            String message = "Failed to parse [" + file + "]!";
            if ((TokenMgrError.class.isInstance(t) || ParseException.class.isInstance(t))
                    && ignoreParsingErrors) {
                logger.debug(message, t);
                return absent();
            }
            if (Error.class.isInstance(t) && !TokenMgrError.class.isInstance(t)) {
                throw Error.class.cast(t);
            }
            throw new RuntimeException(message, t);
        }
    }
}
