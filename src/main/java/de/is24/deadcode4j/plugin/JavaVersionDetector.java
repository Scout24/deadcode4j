package de.is24.deadcode4j.plugin;

import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Detects the Java version of the projects' source code.
 *
 * @since 2.1.0
 */
@Component(role = JavaVersionDetector.class)
public class JavaVersionDetector {
    @Nonnull
    @Requirement
    private LegacySupport legacySupport;

    /**
     * Returns the <tt>-source</tt> argument for the compiler, denormalizing "modern" versions to the traditional
     * notation (e.g. <tt>6</tt> to <tt>1.6</tt>).
     *
     * @throws IllegalStateException if the version cannot be recognized
     */
    public BigDecimal getJavaVersion() throws IllegalStateException {
        String rawVersion = legacySupport.getSession().getCurrentProject().getProperties()
                .getProperty("maven.compiler.source");
        if (rawVersion == null) {
            return new BigDecimal("1.5");
        }
        BigDecimal version;
        try {
            version = new BigDecimal(rawVersion);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Failed to parse Java version [" + rawVersion + "]!", e);
        }
        if (version.scale() == 0) {
            version = version.movePointLeft(version.precision()).add(BigDecimal.ONE);
        }
        return version;
    }

}
