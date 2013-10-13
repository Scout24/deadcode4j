deadcode4j [![Build Status](https://travis-ci.org/ImmobilienScout24/deadcode4j.png)](https://travis-ci.org/ImmobilienScout24/deadcode4j)
==========

*deadcode4j* helps you find code that is no longer used by your application. It is especially useful for cleaning up legacy code.

Usage
-----

Simply run `mvn de.is24.mavenplugins:deadcode4j-maven-plugin:find` in the project you want to analyze.
*deadcode4j* will trigger the _package phase_ to be executed for a project (and for all modules listed in a reactor project) before analyzing the output directories.
The output will look something like this:

    [INFO] --- deadcode4j-maven-plugin:1.0.1:find (default-cli) @ someProject ---
    [INFO] Analyzed 42 class(es).
    [WARNING] Found 2 unused class(es):
    [WARNING]   de.is24.deadcode4j.Foo
    [WARNING]   de.is24.deadcode4j.Bar

Features
--------

*deadcode4j* takes several approaches to analyze if a class is still in usage or not:

- statical code analysis using [Javassist](http://www.jboss.org/javassist/)
- parsing [Spring XML files](http://projects.spring.io/spring-framework/): files ending with `.xml` are examined, each `bean` element's `class` attribute is treated as _live code_
- parsing [`web.xml`](http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd) files: recognizing listed listeners, filters & servlets
- parsing [`*tld`](http://docs.oracle.com/javaee/5/tutorial/doc/bnamu.html) files: recognizing custom tags, tag extra infos, listeners, tag library validators & EL functions

After performing the usage analysis, *deadcode4j* reports which classes are presumably dead.

### False positives

*deadcode4j* knows not everything. Given the approaches listed above, classes reported as being dead may not necessarily be dead. So, don't delete blindly, but double-check the results. Known caveats are:

- Classes being marked with Spring annotations like `@Component`, `@Controller`, `@Service` or `@Repository` usually are not referred to by other bytecode
- *deadcode4j* does not consider test code, so classes used in tests only are deemed to be dead (this is a hint to move such classes to the test src)
- [Java reflection](http://docs.oracle.com/javase/tutorial/reflect/). There's no cure for that.
- As the Java compiler inlines constant expressions, class references may not exist in bytecode; this can be circumvented as outlined at [stackoverflow](http://stackoverflow.com/questions/1833581/when-to-use-intern-on-string-literals)
- Finally, if the analyzed project isn't closed but represents more of a public API or library, expect *deadcode4j* to report many classes which are indeed used by other projects

Obviously, those downsides should and will be tackled by upcoming releases. If you know of any other false positives, please report an [issue](https://github.com/ImmobilienScout24/deadcode4j/issues/new).

Configuration
------------------

If you want to configure the plugin and make use of some of its features, list *deadcode4j* in your `pom.xml`:

    <build>
      <pluginManagement>
        <plugins>
          <plugin>
            <groupId>de.is24.mavenplugins</groupId>
            <artifactId>deadcode4j-maven-plugin</artifactId>
            <version>1.2.0</version>
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
