package de.is24.deadcode4j.plugin.stubs;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

public class ProjectStub extends MavenProjectStub {

    @SuppressWarnings("UnusedDeclaration") // configured via POM
    public void setProject(String project) {
        Build build = new Build();
        build.setOutputDirectory(System.getProperty("java.io.tmpdir") + "/projects/" + project);
        setBuild(build);
    }

}
