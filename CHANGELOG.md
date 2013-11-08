v.1.4-SNAPSHOT
================
Features
--------
* More thorough analysis of `web.xml` files: look for the parameters specified by Spring's [`ContextLoader`](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/context/ContextLoader.html) and [`FrameworkServlet`](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/servlet/FrameworkServlet.html)
    * `contextClass` for an instance of `ConfigurableWebApplicationContext`
    * `contextInitializerClasses` for instances of `ApplicationContextInitializer`
* Mark classes being annotated with the JAXB annotation `javax.xml.bind.annotation.XmlSchema` as _live code_
* Processing of Hibernate Annotations
    * Classes whose members are annotated with [`org.hibernate.annotations.Type`](http://docs.jboss.org/hibernate/annotations/3.5/api/org/hibernate/annotations/Type.html) now depend on
        * either the class defining the associated [`org.hibernate.annotations.TypeDef`](http://docs.jboss.org/hibernate/annotations/3.5/api/org/hibernate/annotations/TypeDef.html)
        * or the class specified as `type` if it is part of the analyzed project
    * Classes being annotated with [`org.hibernate.annotations.GenericGenerator`](http://docs.jboss.org/hibernate/annotations/3.5/api/org/hibernate/annotations/GenericGenerator.html) now depend on the class specified as `strategy` if it is part of the analyzed project
* Mark classes being annotated with those [JSF](https://javaserverfaces.java.net/) as _live code_:
    * `javax.faces.component.behavior.FacesBehavior`
    * `javax.faces.convert.FacesConverter`
    * `javax.faces.event.ListenerFor`
    * `javax.faces.event.ListenersFor`
    * `javax.faces.event.NamedEvent`
    * `javax.faces.render.FacesBehaviorRenderer`
    * `javax.faces.render.FacesRenderer`
    * `javax.faces.validator.FacesValidator`
    * `javax.faces.view.facelets.FaceletsResourceResolver`
* Added possibility to specify which modules should be skipped (configuration parameter __`modulesToSkip`__)
* Mark classes being _direct_ subclasses of [`org.exolab.castor.xml.util.XMLClassDescriptorImpl`](http://castor.codehaus.org/) as _live code_

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.3...master)

v.1.3
================
Features
--------
* Mark classes being annotated with those JEE annotations as _live code_:
    * [`javax.annotation.ManagedBean`](http://docs.oracle.com/javaee/6/api/javax/annotation/ManagedBean.html)
    * [`javax.inject.Named`](http://docs.oracle.com/javaee/6/api/javax/inject/Named.html)
    * [`javax.persistence.metamodel.StaticMetamodel`](http://docs.oracle.com/javaee/6/api/javax/persistence/metamodel/StaticMetamodel.html)
* Mark classes being annotated with those [Spring annotations](http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/beans.html#beans-stereotype-annotations) as _live code_:
    * `org.springframework.stereotype.Component`
    * `org.springframework.stereotype.Controller`
    * `org.springframework.stereotype.Service`
    * `org.springframework.stereotype.Repository`
    * `org.springframework.context.annotation.Configuration`
    * `org.springframework.jmx.export.annotation.ManagedResource`
* Added possibility to specify which annotations mark a class as being _live code_
* Added possibility to specify a custom XML analyzer treating either an element's text or attribute as a used class
* Added goal `find-without-packaging` to speed up execution if need be

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.2.0...deadcode4j-maven-plugin-1.3)

v.1.2.0
================
Features
--------
* Added analysis of `web.xml` files: recognizing listed listeners, filters & servlets as _live code_
* Added analysis of [`*tld`](http://docs.oracle.com/javaee/5/tutorial/doc/bnamu.html) files: recognizing custom tags, tag extra infos, listeners, tag library validators & EL functions as _live code_
* Execute _package_ phase, scan [`webappDirectory/WEB-INF`](http://maven.apache.org/plugins/maven-war-plugin/exploded-mojo.html#webappDirectory) additionally to the output directory
* Mojo is now marked as an aggregator, analyzing all projects of a reactor

Internal
--------
* handle each file independently, i.e. no more setup of a ClassPool & ClassLoader with which to access all classes/files
* use [commons-io](http://commons.apache.org/io/) to iterate over files
* use [Invoker Plugin](http://maven.apache.org/plugins/maven-invoker-plugin/) to test the plugin

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.1.0...deadcode4j-maven-plugin-1.2.0)

v.1.1.0
=======
Features
--------
* Added analysis of [Spring XML files](http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/beans.html#beans-factory-instantiation) to determine if a class is used

Internal
--------
* Integrated with [Travis](https://travis-ci.org/ImmobilienScout24/deadcode4j)

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.0.1...deadcode4j-maven-plugin-1.1.0)

v1.0.1
======
Features
--------
* Added possibility to ignore presumably _dead code_

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode-maven-plugin-1.0.0...deadcode4j-maven-plugin-1.0.1)

v.1.0.0
=======
Features
--------
* Static Class file analysis based on [Javassist](http://www.jboss.org/javassist/)

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/1bf976e7d67d9fa5f142022e6a56bb0d5ab0...deadcode-maven-plugin-1.0.0)
