package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.Analyzer;

/**
 * Analyzes Spring XML files:
 * <ul>
 *     <li>lists the <code>bean</code> classes being referenced</li>
 *     <li>lists the <a href="http://cxf.apache.org/schemas/jaxws.xsd">CXF <code>endpoint</code></a> implementor classes
 *     being referenced</li>
 *     <li>lists the classes executed by
 *     <a href="http://docs.spring.io/spring/docs/3.0.x/reference/scheduling.html#scheduling-quartz-jobdetail">Quartz
 *     jobs</a></li>
 * </ul>
 *
 * @since 1.1.0
 */
public class SpringXmlAnalyzer extends SimpleXmlAnalyzer implements Analyzer {

    public SpringXmlAnalyzer() {
        super("_Spring-XML_", ".xml", "beans");
        registerClassAttribute("bean", "class");
        registerClassAttribute("endpoint", "implementor");
        registerClassAttribute("endpoint", "implementorClass");
        registerClassAttribute("property", "value").withAttributeValue("name", "jobClass");
    }

}
