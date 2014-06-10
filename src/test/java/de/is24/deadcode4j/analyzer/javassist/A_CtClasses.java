package de.is24.deadcode4j.analyzer.javassist;

import de.is24.deadcode4j.junit.FileLoader;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_CtClasses {

    @Test
    public void retrievesAllImplementedInterfaces() throws NotFoundException {
        CtClass clazz = loadCtClass(true, "SubClassOfClassImplementingExternalizable");

        Set<String> implementedInterfaces = CtClasses.getAllImplementedInterfaces(clazz);

        assertThat(implementedInterfaces, containsInAnyOrder("java.io.Externalizable", "java.io.Serializable"));
    }

    @Test
    public void handlesClassLoadingIssuesSilently() throws NotFoundException {
        CtClass clazz = loadCtClass(false, "de.is24.deadcode4j.analyzer.customrepositories.FooRepository");

        Set<String> implementedInterfaces = CtClasses.getAllImplementedInterfaces(clazz);

        assertThat(implementedInterfaces,
                containsInAnyOrder("de.is24.deadcode4j.analyzer.customrepositories.FooRepositoryCustom"));
    }

    private CtClass loadCtClass(boolean useSystemClassPath, String className) throws NotFoundException {
        ClassPool classPool = new ClassPool(useSystemClassPath);
        classPool.appendClassPath(FileLoader.getFile(".").getAbsolutePath());
        CtClass clazz = classPool.getOrNull(className);
        assertThat(clazz, is(notNullValue()));
        return clazz;
    }

}