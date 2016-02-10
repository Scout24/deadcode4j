package de.is24.deadcode4j.plugin;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * <code>CustomXml</code> is used to configure a {@link de.is24.deadcode4j.analyzer.CustomXmlAnalyzer}.
 *
 * @since 1.3
 */
public class CustomXml {

    @java.lang.SuppressWarnings("UnusedDeclaration")
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Set by Plexus when configuring the plugin")
    private String endOfFileName;
    @java.lang.SuppressWarnings("UnusedDeclaration")
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Set by Plexus when configuring the plugin")
    private String rootElement;
    private final List<String> xPaths = newArrayList();

    public String getEndOfFileName() {
        return endOfFileName;
    }

    public String getRootElement() {
        return rootElement;
    }

    public List<String> getXPaths() {
        return xPaths;
    }

}
