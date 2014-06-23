package de.is24.deadcode4j.junit;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AUtilityClass {

    protected abstract Class<?> getType();

    @Test
    public void instantiateClassForTestCoverage() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> clazz = getType();
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
