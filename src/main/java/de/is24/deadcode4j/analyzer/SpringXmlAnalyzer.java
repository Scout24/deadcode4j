package de.is24.deadcode4j.analyzer;

/**
 * Analyzes Spring XML files:
 * <ul>
 * <li>lists the <code>bean</code> classes being referenced</li>
 * <li>lists the classes being referenced by a <a href="http://docs.spring.io/autorepo/docs/spring-framework/3.2.x/javadoc-api/org/springframework/beans/factory/config/MethodInvokingFactoryBean.html">
 * MethodInvokingFactoryBean</a>; note however that the <tt>staticMethod</tt> property is not supported, but the
 * more verbose approach using the <tt>targetClass</tt> and <tt>targetMethod</tt> properties</li>
 * <li>lists the <a href="http://cxf.apache.org/schemas/jaxws.xsd">CXF <code>endpoint</code></a> implementor classes
 * being referenced</li>
 * <li>lists the classes executed by
 * <a href="http://docs.spring.io/spring/docs/3.0.x/reference/scheduling.html#scheduling-quartz-jobdetail">Quartz
 * jobs</a></li>
 * <li>lists the view class used by
 * <a href="http://docs.spring.io/spring/docs/3.0.x/reference/view.html#view-tiles-url">
 * <code>UrlBasedViewResolver</code></a> and subclasses</li>
 * </ul>
 *
 * @since 1.1.0
 */
public class SpringXmlAnalyzer extends ExtendedXmlAnalyzer {

    public SpringXmlAnalyzer() {
        super("_Spring-XML_", ".xml", "beans");
        // regular spring beans
        anyElementNamed("bean").registerAttributeAsClass("class");
        // MethodInvokingFactoryBean stuff
        Path methodInvokingFactoryBean = anyElementNamed("bean").withAttributeValue("class", "org.springframework.beans.factory.config.MethodInvokingFactoryBean");
        registerPropertyValueAsClass(methodInvokingFactoryBean, "targetClass");
        // CXF endpoints
        anyElementNamed("endpoint").registerAttributeAsClass("implementor");
        anyElementNamed("endpoint").anyElementNamed("implementor").registerTextAsClass();
        anyElementNamed("endpoint").registerAttributeAsClass("implementorClass");

        anyElementNamed("property").withAttributeValue("name", "jobClass").registerAttributeAsClass("value");
        anyElementNamed("property").withAttributeValue("name", "viewClass").registerAttributeAsClass("value");
    }

    private void registerPropertyValueAsClass(Path beanPath, String propertyName) {
        Path propertyPath = beanPath.anyElementNamed("property").withAttributeValue("name", propertyName);
        propertyPath.registerAttributeAsClass("value");
        propertyPath.anyElementNamed("value").registerTextAsClass();
    }

}
