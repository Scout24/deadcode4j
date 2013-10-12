v.1.2.0-SNAPSHOT
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

###[code changes](https://github.com/ImmobilienScout24/deadcode4j/compare/deadcode4j-maven-plugin-1.1.0...master)

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
