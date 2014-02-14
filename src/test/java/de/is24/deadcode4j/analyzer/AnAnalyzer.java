package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Rule;

import java.io.File;

public abstract class AnAnalyzer {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();

    protected File getFile(String fileName) {
        String classFile = getClass().getSimpleName() + ".class";
        String pathToClass = getClass().getResource(classFile).getFile();
        String baseDir = pathToClass.substring(0, pathToClass.length() -
                (getClass().getPackage().getName() + "/" + classFile).length());
        return new File(new File(baseDir), fileName);
    }

}
