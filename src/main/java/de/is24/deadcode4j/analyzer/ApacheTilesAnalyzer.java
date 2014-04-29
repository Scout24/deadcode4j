package de.is24.deadcode4j.analyzer;

/**
 * Analyzes <a href="http://tiles.apache.org/">Apache Tiles</a> definition XML files: lists the preparer, bean & item
 * classes being referenced.
 *
 * @since 1.5
 */
public final class ApacheTilesAnalyzer extends SimpleXmlAnalyzer {

    public ApacheTilesAnalyzer() {
        super("_ApacheTilesXml_", ".xml", "tiles-definitions");
        // http://tiles.apache.org/dtds/tiles-config_3_0.dtd
        registerClassAttribute("definition", "preparer");
        // http://tiles.apache.org/dtds/tiles-config_2_1.dtd
        // http://tiles.apache.org/dtds/tiles-config_2_0.dtd
        registerClassAttribute("bean", "classtype");
        registerClassAttribute("item", "classtype");
    }

}
