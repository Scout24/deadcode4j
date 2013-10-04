deadcode4j [![Build Status](https://travis-ci.org/ImmobilienScout24/deadcode4j.png)](https://travis-ci.org/ImmobilienScout24/deadcode4j)
==========

*deadcode4j* helps you find code that is no longer used by your application. It is especially useful for cleaning up legacy code.

Usage
-----

Simply run `mvn de.is24.mavenplugins:deadcode4j-maven-plugin:find` in the project you want to analyze. The output will look something like this:

    [INFO] --- deadcode4j-maven-plugin:1.0.1:find (default-cli) @ someProject ---
    [INFO] Analyzed 42 class(es).
    [WARNING] Found 2 unused class(es):
    [WARNING]   de.is24.deadcode4j.Foo
    [WARNING]   de.is24.deadcode4j.Bar

Features
--------

*deadcode4j* takes several approaches to analyze if a class is still in usage or not:

- statical code analysis using [Javassist](http://www.jboss.org/javassist/)
- parsing [Spring XML files](http://projects.spring.io/spring-framework/)

After performing the usage analysis, *deadcode4j* reports which classes are presumably dead

Configuration
------------------

If you want to configure the plugin and make use of some of its features, list *deadcode4j* in your `pom.xml`:

    <build>
      <pluginManagement>
        <plugins>
          <plugin>
            <groupId>de.is24.mavenplugins</groupId>
            <artifactId>deadcode4j-maven-plugin</artifactId>
            <version>1.1.0</version>
            <configuration>
              <classesToIgnore>
                <!-- Java main class which IS used, I swear -->
                <param>de.is24.deadcode4j.Foo</param>
              </classesToIgnore>
            </configuration>
          </plugin>
        </plugins>
      </pluginManagement>
    </build>

Now it even gets easier: run `mvn deadcode4j:find` and you'll get

    [INFO] --- deadcode4j-maven-plugin:1.0.1:find (default-cli) @ someProject ---
    [INFO] Analyzed 42 class(es).
    [INFO] Ignoring 1 class(es) which seem(s) to be unused.
    [WARNING] Found 1 unused class(es):
    [WARNING]   de.is24.deadcode4j.Bar

_Note that it if you do not intend to bind *deadcode4j* to a lifecycle phase, it is sufficient to define *deadcode4j* under the `pluginManagement` section as listed above._

### Configuration Parameters

-   **classesToIgnore**

    A list of classes that should be ignored by *deadcode4j* (and thus not listed as being dead).
