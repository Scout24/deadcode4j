package de.is24.deadcode4j.analyzer;

/**
 * Analyzes <a href="http://axis.apache.org/axis/java/reference.html#Deployment_WSDD_Reference"><code>.wsdd</code></a>
 * files: lists the defined Axis Service classes being referenced.
 *
 * @since 1.5
 */
public final class WsddAnalyzer extends SimpleXmlAnalyzer {

    public WsddAnalyzer() {
        super("_Axis-WSSD_", ".wsdd", "deployment");
        registerClassAttribute("parameter", "value").withAttributeValue("name", "className");
    }

}
