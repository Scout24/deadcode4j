package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.Module;
import de.is24.deadcode4j.Repository;
import de.is24.deadcode4j.junit.FileLoader;
import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.util.Collections;

public abstract class AnAnalyzer {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();
    protected CodeContext codeContext;

    @Before
    public final void initCodeContext() {
        Module dummyModule = new Module("de.is24:deadcode4j-junit", null, Collections.<File>emptyList(), Collections.<Repository>emptyList());
        codeContext = new CodeContext(dummyModule);
    }

    protected File getFile(String fileName) {
        return FileLoader.getFile(fileName);
    }

}
