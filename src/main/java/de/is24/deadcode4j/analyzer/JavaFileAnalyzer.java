package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import de.is24.deadcode4j.AnalysisContext;
import de.is24.guava.NonNullFunction;
import de.is24.guava.SequentialLoadingCache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import japa.parser.JavaParser;
import japa.parser.TokenMgrError;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;

import javax.annotation.Nonnull;
import java.io.*;

import static com.google.common.base.Optional.of;
import static de.is24.guava.NonNullFunctions.toFunction;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Serves as a base class with which to analyze java files.
 *
 * @since 1.6
 */
public abstract class JavaFileAnalyzer extends AnalyzerAdapter {

    private static final NonNullFunction<AnalysisContext, LoadingCache<File, Optional<CompilationUnit>>> SUPPLIER =
            new NonNullFunction<AnalysisContext, LoadingCache<File, Optional<CompilationUnit>>>() {
                @Nonnull
                @Override
                public LoadingCache<File, Optional<CompilationUnit>> apply(@Nonnull final AnalysisContext analysisContext) {
                    return SequentialLoadingCache.createSingleValueCache(toFunction(new NonNullFunction<File, Optional<CompilationUnit>>() {
                        @Nonnull
                        @Override
                        public Optional<CompilationUnit> apply(@Nonnull File file) {
                            Reader reader = null;
                            try {
                                reader = analysisContext.getModule().getEncoding() != null
                                        ? new InputStreamReader(new FileInputStream(file), analysisContext.getModule().getEncoding())
                                        : new FileReader(file);
                                return of(JavaParser.parse(reader, false));
                            } catch (TokenMgrError e) {
                                throw new RuntimeException("Failed to parse [" + file + "]!", e);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse [" + file + "]!", e);
                            } finally {
                                closeQuietly(reader);
                            }
                        }
                    }));
                }
            };

    @Nonnull
    protected static final String getTypeName(@Nonnull Node node) {
        StringBuilder buffy = new StringBuilder();
        for (;;) {
            if (TypeDeclaration.class.isInstance(node)) {
                if (buffy.length() > 0)
                    buffy.insert(0, '$');
                buffy.insert(0, TypeDeclaration.class.cast(node).getName());
            } else if (CompilationUnit.class.isInstance(node)) {
                final CompilationUnit compilationUnit = CompilationUnit.class.cast(node);
                if (compilationUnit.getPackage() != null) {
                    prepend(compilationUnit.getPackage().getName(), buffy);
                }
            }
            node = node.getParentNode();
            if (node == null) {
                break;
            }
        }
        return buffy.toString();
    }

    @Nonnull
    protected static StringBuilder prepend(@Nonnull NameExpr nameExpr, @Nonnull StringBuilder buffy) {
        for (; ; ) {
            if (buffy.length() > 0) {
                buffy.insert(0, '.');
            }
            buffy.insert(0, nameExpr.getName());
            if (!QualifiedNameExpr.class.isInstance(nameExpr)) {
                break;
            }
            nameExpr = QualifiedNameExpr.class.cast(nameExpr).getQualifier();
        }
        return buffy;
    }

    @Override
    @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "The MavenProject does not provide the proper encoding")
    public final void doAnalysis(@Nonnull AnalysisContext analysisContextntext, @Nonnull File file) {
        if (file.getName().endsWith(".java")) {
            analyzeJavaFile(analysisContextntext, file);
        }
    }


    /**
     * Perform an analysis for the specified java file.
     * Results must be reported via the capabilities of the {@link AnalysisContext}.
     *
     * @since 1.6
     */
    protected abstract void analyzeCompilationUnit(@Nonnull AnalysisContext analysisContext, @Nonnull CompilationUnit compilationUnit);

    private void analyzeJavaFile(@Nonnull AnalysisContext analysisContextntext, @Nonnull File javaFile) {
        CompilationUnit compilationUnit = analysisContextntext.getOrCreateCacheEntry(JavaFileAnalyzer.class, SUPPLIER).getUnchecked(javaFile).get();
        logger.debug("Analyzing Java file [{}]...", javaFile);
        analyzeCompilationUnit(analysisContextntext, compilationUnit);
    }

}
