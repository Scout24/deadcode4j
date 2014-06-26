# deadcode4j 

[![Build Status](https://api.travis-ci.org/ImmobilienScout24/deadcode4j.svg?branch=master)](https://travis-ci.org/ImmobilienScout24/deadcode4j)
[![Coverage Status](https://img.shields.io/coveralls/ImmobilienScout24/deadcode4j.svg?branch=master)](https://coveralls.io/r/ImmobilienScout24/deadcode4j?branch=master)

*deadcode4j* helps you find code that is no longer used by your application. It is especially useful for cleaning up legacy code.

As *deadcode4j* is available via the Maven repository, you can simply run  
`mvn de.is24.mavenplugins:deadcode4j-maven-plugin:find -Dmaven.test.skip=true`  
to analyze your project.  
*deadcode4j* will trigger the _package phase_ to be executed for a project (and for all modules listed in a reactor project) before analyzing the output directories.
The output will look something like this:

    [INFO] --- deadcode4j-maven-plugin:1.5:find (default-cli) @ someProject ---
    [INFO] Analyzed 42 class(es).
    [WARNING] Found 2 unused class(es):
    [WARNING]   de.is24.deadcode4j.Foo
    [WARNING]   de.is24.deadcode4j.Bar

Have a look at the [wiki](https://github.com/ImmobilienScout24/deadcode4j/wiki) to get to know the
[features](https://github.com/ImmobilienScout24/deadcode4j/wiki/deadcode4j-v1.5%3A-Features),
read about the available [goals](https://github.com/ImmobilienScout24/deadcode4j/wiki/deadcode4j-v1.5%3A-Usage),
understand the [configuration](https://github.com/ImmobilienScout24/deadcode4j/wiki/deadcode4j-v1.5%3A-Configuration)
or learn *deadcode4j*'s history and principles.
