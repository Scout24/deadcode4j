# v.1.6-SNAPSHOT
## Features
* ByteCodeAnalyzers know have access to the full class path
    * `AnnotationsAnalyzer` now
        * recursively examines the annotations
        * examines the superclasses for annotations marked with `@Inherited`
    * `InterfacesAnalyzer` now determines implementation even if by a superclass or inherited by other interface
    * `SuperClassAnalyzer` now analyzes the whole class hierarchy
* Introduced `TypeErasureAnalyzer` which finds references that are not found in the byte code due to type erasure
    * references to inner types defined by a superclass or an implemented interface are not recognized; as those types are marked as dependency of the defining *outer* class, they won't show up as false positive
* Added analysis of [Spring Data custom repositories](http://docs.spring.io/spring-data/data-commons/docs/1.6.x/reference/html/repositories.html#repositories.custom-implementations): recognizing custom implementations as _live code_.
    * only recognizes custom implementations following the default naming convention `RepositoryNameImpl`
* made *deadcode4j* more resilient: failing to analyze a file does not lead to termination any longer

## Internal
* calculate class path for each project & pass along to analyzers
    * analyze each module with its own context
    * establish possibility to pass on intermediate results to depending modules

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.5...master)

# v.1.5 [&rarr;announcement](http://sebastiankirsch.blogspot.com/2014/04/deadcode4j-v15-released.html)
## Features
* Added analysis of `aop.xml` files: recognizing listed aspects as _live code_. Supports both [AspectJ](http://eclipse.org/aspectj/) and [AspectWerkz](http://aspectwerkz.codehaus.org/)
* Added analysis of [`.wsdd`](http://axis.apache.org/axis/java/reference.html#Deployment_WSDD_Reference) files: recognizing listed Service classes as _live code_
* Spring XML analysis now also recognizes [CXF endpoint definitions](http://cxf.apache.org/schemas/jaxws.xsd) as _live code_. Supports the `implementor`/`implementorClass` attributes only; if you are using the `implementor` element: use the attribute instead.
* Spring XML analysis now also recognizes classes executed by [Quartz jobs](http://docs.spring.io/spring/docs/3.0.x/reference/scheduling.html#scheduling-quartz-jobdetail) as _live code_.
* Spring XML analysis now also recognizes view classes used by [view resolvers](http://docs.spring.io/spring/docs/3.0.x/reference/view.html#view-tiles-url) as _live code_.
* The custom XML analyzer now allows to specify the predicate `[@attributeName='attributeValue']` in the XPath definition
* introduced goal `find-only` being the equivalent of `find-without-packaging`, except for having a better name
* Added analysis of [`faces-config.xml`](http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_2.xsd) files: recognizing listed classes as _live code_.
* Added analysis of [Spring Web Flow XML](http://www.springframework.org/schema/webflow/spring-webflow-2.0.xsd) files: recognizing listed classes & types as _live code_.
* Added analysis of [Spring XML NamespaceHandlers](http://docs.spring.io/spring/docs/3.2.x/spring-framework-reference/html/extensible-xml.html): recognizing listed namespace handlers as _live code_.
* Hibernate annotations
   * classes annotated with a [`org.hibernate.annotations.GenericGenerator`](http://docs.jboss.org/hibernate/orm/4.2/manual/en-US/html/ch05.html#mapping-declaration-id-generator) that are referred by a class annotated with `javax.persistence.GeneratedValue` are recognized as live code.
   * a warning is issued if a `@TypeDef` is defined more than once with the same name
   * a warning is issued if a `@GenericGenerator` is defined more than once with the same name
* Mark classes being annotated with the JAXB annotation `javax.xml.bind.annotation.XmlRegistry` as _live code_.
* Added analysis of [Apache Tiles](http://tiles.apache.org) XML definition files: recognizing listed classes as _live code_.
* Added analysis of a JEE6 feature: recognizing implementations of [`javax.servlet.ServletContainerInitializer`](http://docs.oracle.com/javaee/6/api/javax/servlet/ServletContainerInitializer.html) as _live code_.
   * support Spring variant of this concept: recognizing implementations of [`org.springframework.web.WebApplicationInitializer`](http://docs.spring.io/spring/docs/3.1.x/javadoc-api/org/springframework/web/WebApplicationInitializer.html) as live code

## Internal
* integrated logging in _tooling_ classes via [SLF4J](http://www.slf4j.org/)
* updated versions of all plugins & dependencies to the most recent version as of 2014/04/11
* use guava'a `Preconditions` for argument checking

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.4.1...deadcode4j-maven-plugin-1.5)

# v.1.4.1
## Features
* introduced new goal `help`

## Internal
* explicitly list all dependencies

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.4...deadcode4j-maven-plugin-1.4.1)

# v.1.4 [&rarr;announcement](http://sebastiankirsch.blogspot.com/2013/11/deadcode4j-v14-released.html)
## Features
* introduced new goal `find-without-packaging`
* More thorough analysis of `web.xml` files: look for the parameters specified by Spring's [`ContextLoader`](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/context/ContextLoader.html) and [`FrameworkServlet`](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/servlet/FrameworkServlet.html)
    * `contextClass` for an instance of `ConfigurableWebApplicationContext`
    * `contextInitializerClasses` for instances of `ApplicationContextInitializer`
* Mark classes being annotated with the JAXB annotation `javax.xml.bind.annotation.XmlSchema` as _live code_
* Processing of Hibernate Annotations
    * Classes whose members are annotated with [`org.hibernate.annotations.Type`](http://docs.jboss.org/hibernate/annotations/3.5/api/org/hibernate/annotations/Type.html) now depend on
        * either the class defining the associated [`org.hibernate.annotations.TypeDef`](http://docs.jboss.org/hibernate/annotations/3.5/api/org/hibernate/annotations/TypeDef.html)
        * or the class specified as `type` if it is part of the analyzed project
    * Classes being annotated with [`org.hibernate.annotations.GenericGenerator`](http://docs.jboss.org/hibernate/annotations/3.5/api/org/hibernate/annotations/GenericGenerator.html) now depend on the class specified as `strategy` if it is part of the analyzed project
* Mark classes being annotated with those [JSF](https://javaserverfaces.java.net/) annotations as _live code_:
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
* Mark classes being __direct__ subclasses of [`org.exolab.castor.xml.util.XMLClassDescriptorImpl`](http://castor.codehaus.org/) as _live code_
* Added possibility to specify which classes mark a __direct__ subclass of those as being _live code_  (configuration parameter __`superClassesMarkingLiveCode`__)
* Added possibility to specify which interfaces being __explicitly__ implemented mark a class as beig _live code_ (configuration parameter __`interfacesMarkingLiveCode`__)

## Internal
* expanded lifecycle of `Analyzer`s: now there's a `finishAnalysis` method being called at the end, enabling post-processing
* refactored `XmlAnalyzer`: only handling the basics now (setting up the SAX parser and checking the file name), introduced `SimpleXmlAnalyzer` providing the old functionality
* defined centralized annotation discovery `ByteCodeAnalyzer.getAnnotations`

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.3...deadcode4j-maven-plugin-1.4)

# v.1.3 [&rarr;announcement](http://sebastiankirsch.blogspot.com/2013/10/deadcode4j-v13-released.html)
## Features
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

# v.1.2.0 [&rarr;announcement](http://sebastiankirsch.blogspot.com/2013/10/deadcode4j-v120-released.html)
## Features
* Added analysis of `web.xml` files: recognizing listed listeners, filters & servlets as _live code_
* Added analysis of [`*tld`](http://docs.oracle.com/javaee/5/tutorial/doc/bnamu.html) files: recognizing custom tags, tag extra infos, listeners, tag library validators & EL functions as _live code_
* Execute _package_ phase, scan [`webappDirectory/WEB-INF`](http://maven.apache.org/plugins/maven-war-plugin/exploded-mojo.html#webappDirectory) additionally to the output directory
* Mojo is now marked as an aggregator, analyzing all projects of a reactor

## Internal
* handle each file independently, i.e. no more setup of a ClassPool & ClassLoader with which to access all classes/files
* use [commons-io](http://commons.apache.org/io/) to iterate over files
* use [Invoker Plugin](http://maven.apache.org/plugins/maven-invoker-plugin/) to test the plugin
* introduced `de.is24.deadcode4j.Utils` class providing convenience functions

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.1.0...deadcode4j-maven-plugin-1.2.0)

# v.1.1.0 [&rarr;announcement](http://sebastiankirsch.blogspot.com/2013/10/introducing-dedcode4j.html)
## Features
* Added analysis of [Spring XML files](http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/beans.html#beans-factory-instantiation) to determine if a class is used

## Internal
* Integrated with [Travis](https://travis-ci.org/ImmobilienScout24/deadcode4j)

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.0.1...deadcode4j-maven-plugin-1.1.0)

# v1.0.1
## Features
* Added possibility to ignore presumably _dead code_

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode-maven-plugin-1.0.0...deadcode4j-maven-plugin-1.0.1)

# v.1.0.0
## Features
* Static Class file analysis based on [Javassist](http://www.jboss.org/javassist/)

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/1bf976e7d67d9fa5f142022e6a56bb0d5ab0...deadcode-maven-plugin-1.0.0)
