package de.is24.deadcode4j.plugin;

import de.is24.deadcode4j.plugin.stubs.ProjectStub;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public final class A_JavaVersionDetector {

    private static class TestScenario extends TestWatcher {
        private JavaVersionDetector objectUnderTest;
        private ProjectStub projectStub;

        @Override
        protected void starting(Description description) {
            objectUnderTest = new JavaVersionDetector();
            MavenSession mavenSession = mock(MavenSession.class);
            projectStub = new ProjectStub();
            when(mavenSession.getCurrentProject()).thenReturn(projectStub);
            LegacySupport legacySupport = mock(LegacySupport.class);
            when(legacySupport.getSession()).thenReturn(mavenSession);
            try {
                ReflectionUtils.setVariableValueInObject(objectUnderTest, "legacySupport", legacySupport);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public void givenMavenCompilerSource(String version) {
            projectStub.getProperties().setProperty("maven.compiler.source", version);
        }

        public BigDecimal whenVersionIsDetected() {
            return objectUnderTest.getJavaVersion();
        }
    }

    public static class Does {
        @Rule
        public final TestScenario testScenario = new TestScenario();

        @Test
        public void detectVersion5IfNothingIsConfigured() {
            BigDecimal version = testScenario.whenVersionIsDetected();

            assertThat(version, is(new BigDecimal("1.5")));
        }

        @Test(expected = IllegalStateException.class)
        public void failsIfConfigurationIsWeird() {
            testScenario.givenMavenCompilerSource("weirdo");

            testScenario.whenVersionIsDetected();
        }
    }

    @RunWith(Parameterized.class)
    public static class RecognizesValidVersions {
        @Rule
        public final TestScenario testScenario = new TestScenario();
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
            testScenario.givenMavenCompilerSource(configuredVersion);

            BigDecimal version = testScenario.whenVersionIsDetected();

            assertThat(version, is(new BigDecimal(expectedVersion)));
        }

    }

}