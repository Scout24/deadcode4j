package de.is24.deadcode4j.analyzer;

/**
 * Analyzes aop.xml files: lists the aspects being referenced.<br/>
 * This should work for both <a href="http://eclipse.org/aspectj/">AspectJ</a> and
 * <a href="http://aspectwerkz.codehaus.org/">AspectWerkz</a>.
 *
 * @since 1.5
 */
public final class AopXmlAnalyzer extends SimpleXmlAnalyzer {

    public AopXmlAnalyzer() {
        super("_AOP-XML_", "aop.xml", null);
        registerClassAttribute("aspect", "class");
        registerClassAttribute("aspect", "name");
    }

}
