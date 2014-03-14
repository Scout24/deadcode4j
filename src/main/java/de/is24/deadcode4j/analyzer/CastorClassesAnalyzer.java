package de.is24.deadcode4j.analyzer;

/**
 * Analyzes class files: marks a class as being in use if it <i>directly</i> extends
 * <code>org.exolab.castor.xml.util.XMLClassDescriptorImpl</code>.
 *
 * @since 1.4
 */
public final class CastorClassesAnalyzer extends SuperClassAnalyzer {

    public CastorClassesAnalyzer() {
        super("_Castor-GeneratedClass_", "org.exolab.castor.xml.util.XMLClassDescriptorImpl");
    }

}
