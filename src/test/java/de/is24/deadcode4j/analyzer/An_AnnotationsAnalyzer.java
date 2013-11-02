package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class An_AnnotationsAnalyzer extends AnAnalyzer {

    private AnnotationsAnalyzer objectUnderTest;

    @Before
    public void setUpObjectUnderTest() {
        this.objectUnderTest = new AnnotationsAnalyzer("junit", "de.is24.deadcode4j.junit.Annotation") {
        };
    }

    @Test
    public void reportsExistenceOfClass() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("AnnotatedClass.class"));

        assertThat("Should analyze one class", codeContext.getAnalyzedCode().getAnalyzedClasses(), hasSize(1));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), contains("AnnotatedClass"));
    }

    @Test
    public void reportsAnnotatedClassAsBeingUsed() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("AnnotatedClass.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have reported class as being in use", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()), hasItem("AnnotatedClass"));
    }

    @Test
    public void doesNotReportUnannotatedClassAsBeingUsed() {
        CodeContext codeContext = new CodeContext();
        objectUnderTest.doAnalysis(codeContext, getFile("SingleClass.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should NOT have reported class as being in use", codeDependencies.size(), is(0));
    }

}
