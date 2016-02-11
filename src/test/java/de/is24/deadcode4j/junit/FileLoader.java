package de.is24.deadcode4j.junit;

import java.io.File;

public class FileLoader {

    /** Returns a file relative to the test classes' directory. */
    public static File getFile(String fileName) {
        Class<FileLoader> fileLoaderClass = FileLoader.class;
        String classFile = fileLoaderClass.getSimpleName() + ".class";
        String pathToClass = fileLoaderClass.getResource(classFile).getFile();
        String baseDir = pathToClass.substring(0, pathToClass.length() -
                (fileLoaderClass.getPackage().getName() + "/" + classFile).length());
        return new File(new File(baseDir), fileName);
    }

}