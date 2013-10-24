package de.is24.deadcode4j.plugin;

import java.util.List;

/**
 * <code>CustomXml</code> is used to configure a {@link de.is24.deadcode4j.analyzer.CustomXmlAnalyzer}.
 *
 * @since 1.3.0
 */
public class CustomXml {

    @SuppressWarnings("UnusedDeclaration")
    private String endOfFileName;
    @SuppressWarnings("UnusedDeclaration")
    private String rootElement;
    @SuppressWarnings("UnusedDeclaration")
    private List<String> xPaths;

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
