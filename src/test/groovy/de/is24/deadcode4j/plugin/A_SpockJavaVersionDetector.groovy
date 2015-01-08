package de.is24.deadcode4j.plugin

import de.is24.deadcode4j.plugin.stubs.ProjectStub
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.LegacySupport
import spock.lang.Specification

class A_SpockJavaVersionDetector extends Specification {

    JavaVersionDetector objectUnderTest;
    ProjectStub projectStub;

    def setup() {
        projectStub = new ProjectStub()
        MavenSession mavenSession = Mock()
        mavenSession.getCurrentProject() >> projectStub
        LegacySupport legacySupport = Mock()
        legacySupport.getSession() >> mavenSession

        objectUnderTest = new JavaVersionDetector()
        objectUnderTest.legacySupport = legacySupport
    }

    def mavenCompilerSource(String version) {
        projectStub.getProperties().setProperty("maven.compiler.source", version);
    }

    def "detects version 5 if nothing is configured"() {
        expect:
        objectUnderTest.getJavaVersion() == new BigDecimal("1.5")
    }

    def "fails if configuration is weird"() {
        given:
        mavenCompilerSource("weird")

        when:
        objectUnderTest.getJavaVersion()

        then:
        thrown(IllegalStateException)
    }

    def "detects appropriate configured version"() {
        mavenCompilerSource(configuredVersion)

        expect:
        objectUnderTest.getJavaVersion() == expectedVersion

        where:
        configuredVersion | expectedVersion
        "1.1"             | 1.1
        "1.2"             | 1.2
        "1.3"             | 1.3
        "1.4"             | 1.4
        "1.5"             | 1.5
        "5"               | 1.5
        "1.6"             | 1.6
        "6"               | 1.6
        "1.7"             | 1.7
        "7"               | 1.7
        "1.8"             | 1.8
        "8"               | 1.8
        "1.9"             | 1.9
        "9"               | 1.9
        "1.42"            | 1.42
        "42"              | 1.42
    }

}