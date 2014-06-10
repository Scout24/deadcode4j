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

    private static LoadingCache<File, Optional<CompilationUnit>> getJavaFileParser(AnalysisContext analysisContext) {
        return analysisContext.getOrCreateCacheEntry(JavaFileAnalyzer.class, SUPPLIER);
    }


    @Override
    @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "The MavenProject does not provide the proper encoding")
    public final void doAnalysis(@Nonnull AnalysisContext analysisContext, @Nonnull File file) {
        if (file.getName().endsWith(".java")) {
            CompilationUnit compilationUnit = getJavaFileParser(analysisContext).getUnchecked(file).get();
            logger.debug("Analyzing Java file [{}]...", file);
            analyzeCompilationUnit(analysisContext, compilationUnit);
        }
    }

    /**
     * Perform an analysis for the specified java file.
     * Results must be reported via the capabilities of the {@link AnalysisContext}.
     *
     * @since 1.6
     */
    protected abstract void analyzeCompilationUnit(@Nonnull AnalysisContext analysisContext, @Nonnull CompilationUnit compilationUnit);

}
