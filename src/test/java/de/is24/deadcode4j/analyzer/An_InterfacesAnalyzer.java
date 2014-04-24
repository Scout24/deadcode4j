package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public final class An_InterfacesAnalyzer extends AnAnalyzer {

    @Test
    public void reportsExistenceOfClasses() {
        Analyzer objectUnderTest = new InterfacesAnalyzer("junit", "java.lang.Cloneable") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("A.class"));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("A"));

        objectUnderTest.doAnalysis(codeContext, getFile("B.class"));
        assertThat(codeContext.getAnalyzedCode().getAnalyzedClasses(), containsInAnyOrder("A", "B"));
    }

    @Test
    public void reportsImplementingClassAsBeingUsed() {
        Analyzer objectUnderTest = new InterfacesAnalyzer("junit", "java.lang.Cloneable", "java.io.Serializable") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingCloneable.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have reported some dependencies!", codeDependencies.size(), is(1));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder("ClassImplementingCloneable", "DeadServlet"));
    }

    @Test
    public void doesNotReportNonImplementingClassAsBeingUsed() {
        Analyzer objectUnderTest = new InterfacesAnalyzer("junit", "java.lang.Cloneable") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingCloneable.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("DeadServlet.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have reported some dependencies!", codeDependencies.size(), is(1));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder("ClassImplementingCloneable"));
    }

    @Test
    public void reportsSubClassImplementingClassAsBeingUsed() {
        Analyzer objectUnderTest = new InterfacesAnalyzer("junit", "java.lang.Runnable") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("SubClassThatShouldBeLive.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have reported some dependencies!", codeDependencies.size(), is(1));
        assertThat(concat(codeDependencies.values()), containsInAnyOrder("SubClassThatShouldBeLive"));
    }

    @Test
    public void reportsClassImplementingSubInterfaceAsBeingUsed() {
        Analyzer objectUnderTest = new InterfacesAnalyzer("junit", "java.io.Serializable") {
        };

        objectUnderTest.doAnalysis(codeContext, getFile("ClassImplementingExternalizable.class"));
        objectUnderTest.doAnalysis(codeContext, getFile("SubClassOfClassImplementingExternalizable.class"));

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have reported some dependencies!", codeDependencies.size(), is(1));
        assertThat(concat(codeDependencies.values()),
                containsInAnyOrder("ClassImplementingExternalizable", "SubClassOfClassImplementingExternalizable"));
    }

}
