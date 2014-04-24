package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import de.is24.deadcode4j.Module;
import de.is24.deadcode4j.Repository;
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
        Module dummyModule = new Module(null, Collections.<File>emptyList(), Collections.<Repository>emptyList());
        codeContext = new CodeContext(dummyModule);
    }

    protected File getFile(String fileName) {
        String classFile = getClass().getSimpleName() + ".class";
        String pathToClass = getClass().getResource(classFile).getFile();
        String baseDir = pathToClass.substring(0, pathToClass.length() -
                (getClass().getPackage().getName() + "/" + classFile).length());
        return new File(new File(baseDir), fileName);
    }

}
