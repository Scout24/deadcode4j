package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class A_JavaFileAnalyzer extends AnAnalyzer<JavaFileAnalyzer> {

    private AtomicBoolean didAnalyzeFile;

    @Before
    public void setUp() {
        this.didAnalyzeFile = new AtomicBoolean(false);
    }

    @Override
    protected JavaFileAnalyzer createAnalyzer() {
        return new JavaFileAnalyzer() {
            @Override
            protected void analyzeCompilationUnit(@Nonnull AnalysisContext analysisContext, @Nonnull CompilationUnit compilationUnit) {
                didAnalyzeFile.set(true);
            }
        };
    }

    @Test
    public void analyzesJavaFiles() {
        analyzeFile("../../src/test/java/SingleClass.java");

        assertThat(didAnalyzeFile.get(), is(true));
    }

    @Test
    public void doesNotAnalyzeNonJavaFile() {
        analyzeFile("spring.xml");

        assertThat(didAnalyzeFile.get(), is(false));
    }

}