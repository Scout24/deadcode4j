package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;

/**
 * Analyzes <a href="http://axis.apache.org/axis/java/reference.html#Deployment_WSDD_Reference"><code>.wsdd</code></a>
 * files: lists the defined Axis Service classes being referenced.
 *
 * @since 1.5
 */
public class WsddAnalyzer extends SimpleXmlAnalyzer implements Analyzer {

    public WsddAnalyzer() {
        super("_Axis-WSSD_", ".wsdd", "deployment");
        registerClassAttribute("parameter", "value").withAttributeValue("name", "className");
    }

}
