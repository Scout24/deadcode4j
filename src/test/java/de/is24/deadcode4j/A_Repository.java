package de.is24.deadcode4j;

import de.is24.deadcode4j.junit.TempFileRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

@SuppressWarnings("ConstantConditions")
public class A_Repository {

    @Rule
    public final TempFileRule tempFileRule = new TempFileRule();

    @Test(expected = NullPointerException.class)
    public void throwsAnExceptionIfTheRepositoryIsNull() throws IOException {
        new Repository(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsAnExceptionIfTheRepositoryIsNoDirectory() throws IOException {
        new Repository(tempFileRule.getTempFile());
    }

}
