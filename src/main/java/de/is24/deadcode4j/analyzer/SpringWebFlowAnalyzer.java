package de.is24.deadcode4j.analyzer;

/**
 * Analyzes <a href="http://www.springframework.org/schema/webflow/spring-webflow-2.0.xsd">Spring Web Flow</a> XML
 * files: lists
 * <ul>
 * <li>a <code>var</code>'s class</li>
 * <li>an <code>attribute</code>'s/<code>input</code>'s/<code>output</code>'s/<code>set</code>'s type</li>
 * <li>the result-type of an <code>evaluate</code></li>
 * </ul>
 * as classes being referenced.
 *
 * @since 1.5
 */
public final class SpringWebFlowAnalyzer extends SimpleXmlAnalyzer {

    public SpringWebFlowAnalyzer() {
        super("_SpringWebFlow-XML_", ".xml", "flow");
        registerClassAttribute("attribute", "type");
        registerClassAttribute("evaluate", "result-type");
        registerClassAttribute("input", "type");
        registerClassAttribute("output", "type");
        registerClassAttribute("set", "type");
        registerClassAttribute("var", "class");
    }

}
