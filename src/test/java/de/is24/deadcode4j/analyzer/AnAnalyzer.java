package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.junit.FileLoader;
import de.is24.deadcode4j.junit.LoggingRule;
import org.junit.Rule;

import java.io.File;

public abstract class AnAnalyzer {

    @Rule
    public final LoggingRule enableLogging = new LoggingRule();

    protected File getFile(String fileName) {
        return FileLoader.getFile(fileName);
    }

}
