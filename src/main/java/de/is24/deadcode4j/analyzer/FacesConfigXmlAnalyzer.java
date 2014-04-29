package de.is24.deadcode4j.analyzer;

/**
 * Analyzes <code>faces-config.xml</code> files: lists an incredible bunch of classes being referenced.
 *
 * @since 1.5
 */
public final class FacesConfigXmlAnalyzer extends SimpleXmlAnalyzer {

    public FacesConfigXmlAnalyzer() {
        super("_faces-config.xml_", "faces-config.xml", "faces-config");
        registerClassElement("action-listener");
        registerClassElement("application-factory");
        registerClassElement("attribute-class");
        registerClassElement("base-name");
        registerClassElement("behavior-class");
        registerClassElement("client-behavior-renderer-class");
        registerClassElement("component-class");
        registerClassElement("converter-class");
        registerClassElement("converter-for-class");
        registerClassElement("el-resolver");
        registerClassElement("exception-handler-factory");
        registerClassElement("external-context-factory");
        registerClassElement("facelet-cache-factory");
        registerClassElement("faces-config-value-classType");
        registerClassElement("faces-context-factory");
        registerClassElement("flash-factory");
        registerClassElement("flow-handler-factory");
        registerClassElement("key-class");
        registerClassElement("lifecycle-factory");
        registerClassElement("managed-bean-class");
        registerClassElement("navigation-handler");
        registerClassElement("partial-view-context-factory");
        registerClassElement("phase-listener");
        registerClassElement("property-class");
        registerClassElement("property-resolver");
        registerClassElement("referenced-bean-class");
        registerClassElement("render-kit-class");
        registerClassElement("render-kit-factory");
        registerClassElement("renderer-class");
        registerClassElement("resource-handler");
        registerClassElement("source-class");
        registerClassElement("state-manager");
        registerClassElement("system-event-class");
        registerClassElement("system-event-listener-class");
        registerClassElement("tag-handler-delegate-factory");
        registerClassElement("validator-class");
        registerClassElement("value-class");
        registerClassElement("variable-resolver");
        registerClassElement("view-declaration-language-factory");
        registerClassElement("view-handler");
        registerClassElement("visit-context-factory");
    }

}
