# deadcode4j [![Latest version](https://maven-badges.herokuapp.com/maven-central/de.is24.mavenplugins/deadcode4j-maven-plugin/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.is24.mavenplugins%22%20AND%20a%3A%22deadcode4j-maven-plugin%22) [![Build Status](https://api.travis-ci.org/ImmobilienScout24/deadcode4j.svg?branch=master)](https://travis-ci.org/ImmobilienScout24/deadcode4j) [![Coverage Status](https://img.shields.io/coveralls/ImmobilienScout24/deadcode4j.svg?branch=master)](https://coveralls.io/r/ImmobilienScout24/deadcode4j?branch=master)

[![Join the chat at https://gitter.im/ImmobilienScout24/deadcode4j](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ImmobilienScout24/deadcode4j?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)



*deadcode4j* helps you find code that is no longer used by your application. It is especially useful for cleaning up legacy code.

As *deadcode4j* is available via the Maven repository, you can simply run  
`mvn de.is24.mavenplugins:deadcode4j-maven-plugin:find -Dmaven.test.skip=true`  
to analyze your project.  
*deadcode4j* will trigger the _package phase_ to be executed for a project (and for all modules listed in a reactor project) before analyzing the output directories.
The output will look something like this:

    [INFO] --- deadcode4j-maven-plugin:2.1.0:find (default-cli) @ someProject ---
    [INFO] Analyzed 42 class(es).
    [WARNING] Found 2 unused class(es):
    [WARNING]   de.is24.deadcode4j.Foo
    [WARNING]   de.is24.deadcode4j.Bar

Have a look at the [wiki](https://github.com/ImmobilienScout24/deadcode4j/wiki) to get to know the
[features](https://github.com/ImmobilienScout24/deadcode4j/wiki/deadcode4j-v2.1.0%3A-Features),
read about the available [goals](https://github.com/ImmobilienScout24/deadcode4j/wiki/deadcode4j-v2.1.0%3A-Usage),
understand the [configuration](https://github.com/ImmobilienScout24/deadcode4j/wiki/deadcode4j-v2.1.0%3A-Configuration)
or learn *deadcode4j*'s history and principles.

*deadcode4j* is tested with Maven 3.0.5, 3.1.1, 3.2.5 & 3.3.9.
