package de.is24.deadcode4j.analyzer.javassist;

import de.is24.deadcode4j.junit.AUtilityClass;
import de.is24.deadcode4j.junit.FileLoader;
import de.is24.javassist.CtClasses;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class A_CtClasses extends AUtilityClass {

	private ClassPath classPath;

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

	@Test
	public void silentlyFailsForMissingNestedClasses() throws NotFoundException {
		CtClass clazz = loadCtClass(false, "ClassWithInnerClass");
		trickJavassistByRemovingClassPath(clazz);

		Iterable<CtClass> nestedClasses = CtClasses.getNestedClassesOf(clazz);

		assertThat(nestedClasses, is(emptyIterable()));
	}

	@Test
	public void silentlyFailsForMissingDeclaringClass() throws NotFoundException {
		CtClass clazz = loadCtClass(false, "ClassWithInnerClass$InnerClass");
		trickJavassistByRemovingClassPath(clazz);

		Iterable<CtClass> declaringClasses = CtClasses.getDeclaringClassesOf(clazz);

		assertThat(declaringClasses, is(Matchers.<CtClass>iterableWithSize(1)));
	}

	@Override
	protected Class<?> getType() {
		return CtClasses.class;
    }

	private CtClass loadCtClass(boolean useSystemClassPath, String className) throws NotFoundException {
		ClassPool classPool = new ClassPool(useSystemClassPath);
		classPath = classPool.appendClassPath(FileLoader.getFile(".").getAbsolutePath());
		CtClass clazz = classPool.getOrNull(className);
		assertThat(clazz, is(notNullValue()));
		return clazz;
	}

	private void trickJavassistByRemovingClassPath(CtClass clazz)
			throws NotFoundException {
		clazz.getClassFile2(); // initialize inner cache for this class to prevent RuntimeException
		clazz.getClassPool().removeClassPath(classPath);
    }

}