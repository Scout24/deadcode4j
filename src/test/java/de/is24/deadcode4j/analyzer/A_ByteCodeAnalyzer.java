package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import javassist.CtClass;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_ByteCodeAnalyzer extends AnAnalyzer {

    private ByteCodeAnalyzer objectUnderTest;

    @Before
    public void setUpObjectUnderTest() {
        this.objectUnderTest = new ByteCodeAnalyzer() {
            @Override
            protected void analyzeClass(@Nonnull CodeContext codeContext, @Nonnull CtClass clazz) {
                codeContext.addAnalyzedClass(clazz.getName());
            }
        };
    }

    @Test
    public void analyzesAClassFile() {
        CodeContext codeContext = new CodeContext();

        objectUnderTest.doAnalysis(codeContext, getFile("SingleClass.class"));

        assertThat("Should analyze one class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), contains("SingleClass"));
    }

    @Test
    public void doesNotAnalyzeNonClassFile() {
        CodeContext codeContext = new CodeContext();

        objectUnderTest.doAnalysis(codeContext, getFile("spring.xml"));

        assertThat("Should analyze no class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(0));
        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should analyze no class", codeDependencies.size(), is(0));
    }

}
