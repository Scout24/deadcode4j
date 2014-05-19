package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.AnalysisContext;
import javassist.CtClass;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public final class A_ByteCodeAnalyzer extends AnAnalyzer<ByteCodeAnalyzer> {

    @Override
    protected ByteCodeAnalyzer createAnalyzer() {
        return new ByteCodeAnalyzer() {
            @Override
            protected void analyzeClass(@Nonnull AnalysisContext analysisContext, @Nonnull CtClass clazz) {
                analysisContext.addAnalyzedClass(clazz.getName());
            }
        };
    }

    @Test
    public void analyzesAClassFile() {
        analyzeFile("SingleClass.class");

        assertThatClassesAreReported("SingleClass");
        assertThatNoDependenciesAreReported();
    }

    @Test
    public void doesNotAnalyzeNonClassFile() {
        analyzeFile("spring.xml");

        assertThat("Should analyze no class", analysisContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(0));
        assertThatNoDependenciesAreReported();
    }
}
