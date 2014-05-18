package de.is24.deadcode4j.analyzer;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import de.is24.deadcode4j.CodeContext;
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

    private static final NonNullFunction<CodeContext, LoadingCache<File, Optional<CompilationUnit>>> SUPPLIER =
            new NonNullFunction<CodeContext, LoadingCache<File, Optional<CompilationUnit>>>() {
                @Nonnull
                @Override
                public LoadingCache<File, Optional<CompilationUnit>> apply(@Nonnull final CodeContext codeContext) {
                    return SequentialLoadingCache.createSingleValueCache(toFunction(new NonNullFunction<File, Optional<CompilationUnit>>() {
                        @Nonnull
                        @Override
                        public Optional<CompilationUnit> apply(@Nonnull File file) {
                            Reader reader = null;
                            try {
                                reader = codeContext.getModule().getEncoding() != null
                                        ? new InputStreamReader(new FileInputStream(file), codeContext.getModule().getEncoding())
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

    @Override
    @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "The MavenProject does not provide the proper encoding")
    public final void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull File file) {
        if (file.getName().endsWith(".java")) {
            analyzeJavaFile(codeContext, file);
        }
    }


    /**
     * Perform an analysis for the specified java file.
     * Results must be reported via the capabilities of the {@link CodeContext}.
     *
     * @since 1.6
     */
    protected abstract void analyzeCompilationUnit(@Nonnull CodeContext codeContext, @Nonnull CompilationUnit compilationUnit);

    private void analyzeJavaFile(@Nonnull CodeContext codeContext, @Nonnull File javaFile) {
        CompilationUnit compilationUnit = codeContext.getOrCreateCacheEntry(JavaFileAnalyzer.class, SUPPLIER).getUnchecked(javaFile).get();
        logger.debug("Analyzing Java file [{}]...", javaFile);
        analyzeCompilationUnit(codeContext, compilationUnit);
    }

}
