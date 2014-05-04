package de.is24.deadcode4j.plugin.stubs;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import static com.google.common.collect.Lists.newArrayList;

public class ProjectStub extends MavenProjectStub {

    public ProjectStub() {
        ArtifactStub artifact = new ArtifactStub();
        artifact.setGroupId("de.is24.junit");
        artifact.setArtifactId("project");
        artifact.setVersion("42");
        setArtifact(artifact);

        setCompileSourceRoots(newArrayList("src/test/java/"));
    }

    @SuppressWarnings("UnusedDeclaration") // configured via POM
    public void setOutputDirectory(String directory) {
        Build build = new Build();
        build.setOutputDirectory(directory);
        setBuild(build);
    }

}
