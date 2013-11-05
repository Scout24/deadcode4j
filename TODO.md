Features
--------
* Hibernate annotations
   * org.hibernate.annotations.GenericGenerator(strategy)
   * org.hibernate.annotations.Type(type)
   * The class using `javax.persistence.GeneratedValue` depends on the class defining the associated `org.hibernate.annotations.GenericGenerator`
* Spring Web Flow XML
* [CXF XML endpoint definitions](http://cxf.apache.org/schemas/jaxws.xsd)
* JBoss XML files
* Aspectj XML files
* Spring/Quartz XML bean definition
* Custom Subclass definition
* _Detection 2.0_
    * check if a Spring bean is really used
    * check if JSF stuff is really used
* Class.forName
* JSP imports
* recognize cyclic dependencies
* generate a report

Internals
---------
* integrate logging; candidate for slf4j is http://code.google.com/p/slf4j-maven-plugin-log (multithreading issue though)
