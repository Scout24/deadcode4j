package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import de.is24.deadcode4j.CodeContext;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public final class An_InterfaceAnalyzer extends AnAnalyzer {

    @Test
    public void reportsExistenceOfClasses() {
        Analyzer objectUnderTest = new InterfaceAnalyzer("junit", "java.lang.Cloneable") {
        };
        CodeContext codeContext = new CodeContext();

        objectUnderTest.doAnalysis(codeContext, getFile("A.class"));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("A"));

        objectUnderTest.doAnalysis(codeContext, getFile("B.class"));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("A", "B"));
    }

    @Test
    public void reportsAnnotatedClassAsBeingUsed() {
        Analyzer objectUnderTest = new InterfaceAnalyzer("junit", "java.lang.Cloneable", "java.io.Serializable") {
        };
        CodeContext codeContext = new CodeContext();

        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingCloneable.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingSerializable.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have reported some dependencies!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("ClassImplementingCloneable", "ClassImplementingSerializable"));
    }

    @Test
    public void doesNotReportUnannotatedClassAsBeingUsed() {
        Analyzer objectUnderTest = new InterfaceAnalyzer("junit", "java.lang.Cloneable") {
        };
        CodeContext codeContext = new CodeContext();

        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingCloneable.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingSerializable.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have reported some dependencies!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("ClassImplementingCloneable"));
    }

}
