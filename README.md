# deadcode4j 

[![Build Status](https://api.travis-ci.org/ImmobilienScout24/deadcode4j.svg?branch=master)](https://travis-ci.org/ImmobilienScout24/deadcode4j)
[![Coverage Status](https://img.shields.io/coveralls/ImmobilienScout24/deadcode4j.svg?branch=master)](https://coveralls.io/r/ImmobilienScout24/deadcode4j)

*deadcode4j* helps you find code that is no longer used by your application. It is especially useful for cleaning up legacy code.

## Usage
### `deadcode4j-maven-plugin:find`
Simply run `mvn de.is24.mavenplugins:deadcode4j-maven-plugin:find -Dmaven.test.skip=true` in the project you want to analyze.
*deadcode4j* will trigger the _package phase_ to be executed for a project (and for all modules listed in a reactor project) before analyzing the output directories.
The output will look something like this:

    [INFO] --- deadcode4j-maven-plugin:1.5:find (default-cli) @ someProject ---
    [INFO] Analyzed 42 class(es).
    [WARNING] Found 3 unused class(es):
    [WARNING]   de.is24.deadcode4j.Foo
    [WARNING]   de.is24.deadcode4j.Bar
    [WARNING]   de.is24.deadcode4j.SomeAnnotatedClass
    
_The `-Dmaven.test.skip=true` part skips compiling & executing tests, as they are not relevant for the analysis._

### `deadcode4j-maven-plugin:find-only`
As an alternative, you can run `mvn de.is24.mavenplugins:deadcode4j-maven-plugin:find-only` which performs the same analysis, but without triggering the _package phase_. This is intended for repeated analysis (e.g. after updating the configuration) or if your project is already packaged.

### `deadcode4j-maven-plugin:help`
Lists the available goals & parameters.

### ~~`deadcode4j-maven-plugin:find-without-packaging`~~
_This goal is deprecated. It is replaced by `de.is24.mavenplugins:deadcode4j-maven-plugin:find-only` which has a shorter name._

## Features
*deadcode4j* takes several approaches to analyze if a class is still in use or not:

- statical code analysis using [Javassist](http://www.jboss.org/javassist/), recognizing class dependencies
- parsing [Spring XML files](http://projects.spring.io/spring-framework/): files ending with `.xml` are examined
    - each `bean` element's `class` attribute is treated as _live code_
    - [CXF endpoint definitions](http://cxf.apache.org/schemas/jaxws.xsd): each `endpoint` element's `implementor`/`implementorClass` attribute is treated as _live code_
    - [Quartz job definitions](http://docs.spring.io/spring/docs/3.0.x/reference/scheduling.html#scheduling-quartz-jobdetail): each `property` element's `class` attribute is treated as _live code_ if it has an `name` attribute of `jobClass`
    - [Spring View resolvers](http://docs.spring.io/spring/docs/3.0.x/reference/view.html#view-tiles-url): each `property` element's `class` attribute is treated as _live code_ if it has an `name` attribute of `viewClass`
    - recognizes [Spring XML NamespaceHandlers](http://docs.spring.io/spring/docs/3.2.x/spring-framework-reference/html/extensible-xml.html) as _live code_
- recognizing classes annotated with those [Spring annotations](http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/beans.html#beans-stereotype-annotations) as _live code_:
    - `org.springframework.context.annotation.Configuration`
    - `org.springframework.jmx.export.annotation.ManagedResource`
    - `org.springframework.stereotype.Component`
    - `org.springframework.stereotype.Controller`
    - `org.springframework.stereotype.Service`
    - `org.springframework.stereotype.Repository`
- parsing `web.xml`
    - recognizing listed listeners, filters & servlets (according to the [XSD](http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd) files)
    - look for the parameters defined by Spring's [ContextLoader](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/context/ContextLoader.html) and [FrameworkServlet](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/servlet/FrameworkServlet.html): `contextClass` & `contextInitializerClasses`, treating the configured classes as _live code_
    - if the `metadata-complete` attribute isn't explicitly set to `false`:
        - implementations of [`javax.servlet.ServletContainerInitializer`](http://docs.oracle.com/javaee/6/api/javax/servlet/ServletContainerInitializer.html) are treated as _live code_
        - implementations of [`org.springframework.web.WebApplicationInitializer`](http://docs.spring.io/spring/docs/3.1.x/javadoc-api/org/springframework/web/WebApplicationInitializer.html) are treated as _live code_
- parsing [`*.tld`](http://docs.oracle.com/javaee/5/tutorial/doc/bnamu.html) files: recognizing custom tags, tag extra infos, listeners, tag library validators & EL functions
- parsing [`faces-config.xml`](http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_2.xsd) files: recognizing those element classes as _live code_: `action-listener`, `application-factory`, `attribute-class`, `base-name`, `behavior-class`, `client-behavior-renderer-class`, `component-class`, `converter-class`, `converter-for-class`, `el-resolver`, `exception-handler-factory`, `external-context-factory`, `facelet-cache-factory`, `faces-config-value-classType`, `faces-context-factory`, `flash-factory`, `flow-handler-factory`, `key-class`, `lifecycle-factory`, `managed-bean-class`, `navigation-handler`, `partial-view-context-factory`, `phase-listener`, `property-class`, `property-resolver`, `referenced-bean-class`, `render-kit-class`, `render-kit-factory`, `renderer-class`, `resource-handler`, `source-class`, `state-manager`, `system-event-class`, `system-event-listener-class`, `tag-handler-delegate-factory`, `validator-class`, `value-class`, `variable-resolver`, `view-declaration-language-factory`, `view-handler`, `visit-context-factory`
- recognizing classes annotated with JEE annotations as _live code_:
    - [`javax.annotation.ManagedBean`](http://docs.oracle.com/javaee/6/api/javax/annotation/ManagedBean.html)
    - [`javax.inject.Named`](http://docs.oracle.com/javaee/6/api/javax/inject/Named.html)
    - [`javax.persistence.metamodel.StaticMetamodel`](http://docs.oracle.com/javaee/6/api/javax/persistence/metamodel/StaticMetamodel.html)
    - JAXB annotation [`javax.xml.bind.annotation.XmlRegistry`](http://docs.oracle.com/javaee/6/api/javax/xml/bind/annotation/XmlRegistry.html)
    - JAXB annotation [`javax.xml.bind.annotation.XmlSchema`](http://docs.oracle.com/javaee/6/api/javax/xml/bind/annotation/XmlSchema.html)
    - [JSF](https://javaserverfaces.java.net/) annotations `javax.faces.component.behavior.FacesBehavior`, `javax.faces.convert.FacesConverter`, `javax.faces.event.ListenerFor`, `javax.faces.event.ListenersFor`, `javax.faces.event.NamedEvent`, `javax.faces.render.FacesBehaviorRenderer`, `javax.faces.render.FacesRenderer`, `javax.faces.validator.FacesValidator`, `javax.faces.view.facelets.FaceletsResourceResolver`
- parsing [Spring Web Flow XML](http://www.springframework.org/schema/webflow/spring-webflow-2.0.xsd): files ending with `.xml` are examined
    - each `attribute` element's `type` attribute is treated as _live code_
    - each `evaluate` element's `result-type` attribute is treated as _live code_
    - each `input` element's `type` attribute is treated as _live code_
    - each `output` element's `type` attribute is treated as _live code_
    - each `set` element's `type` attribute is treated as _live code_
    - each `var` element's `class` attribute is treated as _live code_
- parsing [Apache Tiles](http://tiles.apache.org) XML definition files: files ending with `.xml` are examined
    - each `definition` element's `preparer` attribute is treated as _live code_
    - each `bean` element's `classtype` attribute is treated as _live code_
    - each `item` element's `classtype` attribute is treated as _live code_
- processing [Hibernate Annotations](http://docs.jboss.org/hibernate/annotations/3.5/reference/en/html/)
    - recognizing the `strategy` value of the [`org.hibernate.annotations.GenericGenerator`](http://docs.jboss.org/hibernate/annotations/3.5/api/org/hibernate/annotations/GenericGenerator.html) annotation as _live code_
        - issue a warning if a `@GenericGenerator` is defined more than once with the same `name`
    - recognizing classes annotated with [`org.hibernate.annotations.GenericGenerator`](http://docs.jboss.org/hibernate/orm/4.2/manual/en-US/html/ch05.html#mapping-declaration-id-generator) that are referenced by another class via [`javax.persistence.GeneratedValue`](http://docs.oracle.com/javaee/6/api/javax/persistence/GeneratedValue.html) annotation's `generator` value as _live code_
    - recognizing the `type` value of the [`org.hibernate.annotations.Type`](http://docs.jboss.org/hibernate/annotations/3.5/api/org/hibernate/annotations/Type.html) annotation as _live code_
    - recognizing classes annotated with a [`org.hibernate.annotations.TypeDef`](http://docs.jboss.org/hibernate/annotations/3.5/api/org/hibernate/annotations/TypeDef.html) that are referenced by another class in the project as _live code_
        - issue a warning if a `@TypeDef` is defined more than once with the same `name`
- recognizing `*Descriptor` classes being generated by [Castor](http://castor.codehaus.org/) as _live code_
- recognizing service classes defined within [Axis `.wsdd` files](http://axis.apache.org/axis/java/reference.html#Deployment_WSDD_Reference) as _live code_
- recognizing aspects defined within `aop.xml` as _live code_; supports both [AspectJ](http://eclipse.org/aspectj/) and [AspectWerkz](http://aspectwerkz.codehaus.org/)
- Customization
    - recognizing classes annotated with custom specified annotations as _live code_
    - recognizing classes *directly*<sup>1</sup> implementing custom specified interfaces as _live code_
    - recognizing classes *directly*<sup>2</sup> extending custom specified classes as _live code_
    - custom XML file parsing: treating classes referenced in elements' text or attributes as _live code_

After performing the usage analysis, *deadcode4j* reports which classes are presumably dead.

> <sup>1</sup> When examining `class C extends B implements A`, only `B` is recognized as implementing `A`; you can circumvent this by defining `C extends B implements A`  
> <sup>2</sup> When examining `class C extends B extends A`, only `B` is recognized as subclass of `A`; no circumvention here - create an issue if you think this is absolutely required!

### Limitations
- Inner classes are always recognized as being referenced by the outer class and vice versa (even static inner classes). _This is not only true for the currently used Javassist, but also for BCEL. This suggests that this is an aspect of the JVM spec._

### False positives

*deadcode4j* knows not everything. Given the approaches listed above, classes reported as being dead may not necessarily be dead. So, don't delete blindly, but double-check the results. Known caveats are:

- *deadcode4j* does not consider test code, so classes used in tests only are deemed to be dead (this is a hint to move such classes to the test src)
- [Java reflection](http://docs.oracle.com/javase/tutorial/reflect/). There's no cure for that.
- Classes with a `main` method are not recognized as _dead code_ - the reason being that legacy code tends to have test routines defined in the the `main` method. You may ignore those, have a look at the configuration section below.
- As the Java compiler inlines constant expressions, class references may not exist in bytecode; this can be circumvented as outlined at [stackoverflow](http://stackoverflow.com/questions/1833581/when-to-use-intern-on-string-literals)
- The Java compiler also does something called [type erasure](http://docs.oracle.com/javase/tutorial/java/generics/erasure.html), thus a class defining a field `private List<Foo> = new List<>();` does not depend on the class `Foo` on bytecode level - which is the basis of *deadcode4j*'s dependency analysis
- Finally, if the analyzed project isn't closed but represents more of a public API or library, expect *deadcode4j* to report many classes which are indeed used by other projects

If you know of any other false positives, please report an [issue](https://github.com/ImmobilienScout24/deadcode4j/issues/new).

In general, I recommend using [ack](http://beyondgrep.com/) to manually double-check if a class is really dead.

## Configuration

If you want to configure the plugin and make use of some of its features, list *deadcode4j* in your `pom.xml`:

    <build>
      <pluginManagement>
        <plugins>
          <plugin>
            <groupId>de.is24.mavenplugins</groupId>
            <artifactId>deadcode4j-maven-plugin</artifactId>
            <version>1.5</version>
            <configuration>
              <annotationsMarkingLiveCode>
                <param>de.is24.deadcode4j.LiveCode</param>
              </annotationsMarkingLiveCode>
              <classesToIgnore>
                <!-- Java main class which IS used, I swear -->
                <param>de.is24.deadcode4j.Foo</param>
              </classesToIgnore>
            </configuration>
          </plugin>
        </plugins>
      </pluginManagement>
    </build>

As *deadcode4j* uses [semantic versioning](http://semver.org/)<sup>3</sup>, you could (theoretically) safely define the version as `<version>[1.4,1.5)</version>` (if the Maven installation you are using supports version ranges) to benefit from bugfixes immediately. However, [MNG-2742](http://jira.codehaus.org/browse/MNG-2742) prevents you to use version ranges for plugins. :(

Now run `mvn de.is24.mavenplugins:deadcode4j-maven-plugin:find-without-packaging` and you'll get

    [INFO] --- deadcode4j-maven-plugin:1.5:find-without-packaging (default-cli) @ someProject ---
    [INFO] Analyzed 42 class(es).
    [INFO] Ignoring 1 class(es) which seem(s) to be unused.
    [WARNING] Found 1 unused class(es):
    [WARNING]   de.is24.deadcode4j.Bar

_Note that it if you do not intend to bind *deadcode4j* to a lifecycle phase, it is sufficient to define *deadcode4j* under the `pluginManagement` section as listed above._

> <sup>3</sup> in its current state, *deadcode4j* considers its public API to be its Maven Plugin capabilities, i.e. the goals & configuration will be (semantically) stable as long as there is no major version change.
If you look at its internals, the names, signatures & semantics of the defined classes (like e.g. `de.is24.deadcode4j.analyzer.SimpleXmlAnalyzer`) may very well change, as they are NOT considered to be part of the public API (even though they are `public` classes).  
That said, if you simply use *deadcode4j* as a Maven Plugin, you could even define the version as `[1,2)` - if Maven would allow version ranges for plugins; if you mess with the code however, better stick to a defined version (as usual).

### Configuration Parameters

- **annotationsMarkingLiveCode**

    A list of fully qualified annotation classes which, if applied, mark classes as being _live code_

- **classesToIgnore**

    A list of fully qualified classes that should be ignored by *deadcode4j* (and thus not listed as being dead).

-   **customXmls**

    A list of definitions on how to analyze custom XML:

        <customXmls>
          <customXml>
            <endOfFileName>.xml</endOfFileName>
            <rootElement>root</rootElement>
            <xPaths>
              <param>element/text()</param>
              <param>anotherElement[@attributeValue='mandatory']/text()</param>
              <param>thirdElement/@attribute</param>
            </xPaths>
          </customXml>
          <!-- more custom XML -->
        </customXmls>

    -   **endOfFileName** _(mandatory)_

        The file suffix that needs to be matched in order to analyze a file

    -   **rootElement**

        The name of the root element that needs to be matched in order to analyze an XML file; specifying a root element speeds up the analysis

    -   **xPaths** _(mandatory)_

        A list of XPath definitions identifying an XML node which is to be recognized as a class being in use.
        Supported expressions are: `element/text()` and `element/@attribute`; an `element` can be restricted to have a specific attribute value by defining `element[@requiredAttribute='requiredValue']`

- **interfacesMarkingLiveCode**

    A list of fully qualified interface names which, if implemented, mark classes as being _live code_
    _see limitations mentioned in the **Features** section_

- **modulesToSkip**

    A list of the modules to skip

- **superClassesMarkingLiveCode**

    A list of fully qualified class names which, if extended, mark classes as being _live code_
    _see limitations mentioned in the **Features** section_

## In closing
Read [here](http://sebastiankirsch.blogspot.com/2013/10/introducing-dedcode4j.html) how it all started.
