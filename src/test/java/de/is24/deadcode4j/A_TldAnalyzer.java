package de.is24.deadcode4j;

import javassist.ClassPool;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public final class A_TldAnalyzer {

    @Test
    public void shouldParseTldFiles() {
        TldAnalyzer objectUnderTest = new TldAnalyzer();

        CodeContext codeContext = new CodeContext(getClass().getClassLoader(), mock(ClassPool.class));
        objectUnderTest.doAnalysis(codeContext, "taglib.tld");

        Map<String, ? extends Iterable<String>> codeDependencies = codeContext.getAnalyzedCode().getCodeDependencies();
        assertThat("Should have analyzed the TLD file!", codeDependencies.size(), is(1));
        assertThat(getOnlyElement(codeDependencies.values()),
                containsInAnyOrder("TagClass", "TagExtraInfo", "TagLibraryValidator", "TldFunction", "WebAppListener"));
    }

}
