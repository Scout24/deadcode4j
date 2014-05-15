package de.is24.deadcode4j.analyzer;

import org.junit.Test;

public abstract class AByteCodeAnalyzer<T extends ByteCodeAnalyzer> extends AnAnalyzer<T> {

    @Test
    public void reportsExistenceOfClasses() {
        analyzeFile("A.class");
        assertThatClassesAreReported("A");

        analyzeFile("B.class");
        assertThatClassesAreReported("A", "B");
    }

}
