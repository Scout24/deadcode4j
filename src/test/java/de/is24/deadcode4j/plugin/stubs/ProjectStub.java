package de.is24.deadcode4j.plugin.stubs;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import java.util.Properties;

import static com.google.common.collect.Lists.newArrayList;

public class ProjectStub extends MavenProjectStub {

    private final Properties properties = super.getProperties();

    public ProjectStub() {
        ArtifactStub artifact = new ArtifactStub();
        artifact.setGroupId("de.is24.junit");
        artifact.setArtifactId("project");
        artifact.setVersion("42");
        setArtifact(artifact);
        setGroupId(artifact.getGroupId());
        setArtifactId(artifact.getArtifactId());
        setVersion(artifact.getVersion());
        setPackaging("jar");

        setCompileSourceRoots(newArrayList("src/test/java/"));

        properties.setProperty("project.build.sourceEncoding", "UTF-8");
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @SuppressWarnings("UnusedDeclaration") // configured via POM
    public void setOutputDirectory(String directory) {
        Build build = new Build();
        build.setOutputDirectory(directory);
        setBuild(build);
    }

}
