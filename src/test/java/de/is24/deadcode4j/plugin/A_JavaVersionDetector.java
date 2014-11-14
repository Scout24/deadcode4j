package de.is24.deadcode4j.plugin;

import de.is24.deadcode4j.plugin.stubs.ProjectStub;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public final class A_JavaVersionDetector {

    public static class Does {
        @Test
        public void detectVersion5IfNothingIsConfigured() {
            JavaVersionDetector objectUnderTest = new JavaVersionDetector(new ProjectStub());

            BigDecimal version = objectUnderTest.getJavaVersion();

            assertThat(version, is(new BigDecimal("1.5")));
        }

        @Test(expected = IllegalStateException.class)
        public void failsIfConfigurationIsWeird() {
            ProjectStub projectStub = new ProjectStub();
            projectStub.getProperties().setProperty("maven.compiler.source", "weirdo");
            JavaVersionDetector objectUnderTest = new JavaVersionDetector(projectStub);

            objectUnderTest.getJavaVersion();
        }
    }

    @RunWith(Parameterized.class)
    public static class RecognizesValidVersions {
        @Parameterized.Parameter(0)
        public String configuredVersion;
        @Parameterized.Parameter(1)
        public String expectedVersion;

        @Parameterized.Parameters(name = "configured version [{0}] is resolved as [{1}]")
        public static Iterable<Object[]> data() {
            return asList(new Object[][]{
                    {"1.1", "1.1"},
                    {"1.2", "1.2"},
                    {"1.3", "1.3"},
                    {"1.4", "1.4"},
                    {"1.5", "1.5"},
                    {"5", "1.5"},
                    {"1.6", "1.6"},
                    {"6", "1.6"},
                    {"1.7", "1.7"},
                    {"7", "1.7"},
                    {"1.8", "1.8"},
                    {"8", "1.8"},
                    {"1.9", "1.9"},
                    {"9", "1.9"},
                    {"1.42", "1.42"},
                    {"42", "1.42"}
            });
        }

        @Test
        public void detectsConfiguredVersion() {
            ProjectStub projectStub = new ProjectStub();
            projectStub.getProperties().setProperty("maven.compiler.source", configuredVersion);
            JavaVersionDetector objectUnderTest = new JavaVersionDetector(projectStub);

            BigDecimal calculatedVersion = objectUnderTest.getJavaVersion();

            assertThat(calculatedVersion, is(new BigDecimal(expectedVersion)));
        }

    }

}