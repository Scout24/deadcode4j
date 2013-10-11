package de.is24.deadcode4j.analyzer;

import java.io.File;

abstract class AnAnalyzer {

    protected File getFile(String fileName) {
        String classFile = getClass().getSimpleName() + ".class";
        String pathToClass = getClass().getResource(classFile).getFile();
        String baseDir = pathToClass.substring(0, pathToClass.length() -
                (getClass().getPackage().getName() + "/" + classFile).length());
        return new File(new File(baseDir), fileName);
    }

}
