Features
--------
* set up all Maven dependencies to allow analysis of indirectly extended super classes and implemented interfaces (currently, only the immediate super class and explicitly implemented interfaces are recognized)
* recognize [custom Spring Data repositories](http://docs.spring.io/spring-data/data-commons/docs/1.6.x/reference/html/repositories.html#repositories.custom-implementations)
** this requires to know if a class implements `Repository`, even recursively - which in turn means it is required to set up all dependencies
** as a workaround, one can define the custom implementation as a regular Spring bean (as suggested [here](http://docs.spring.io/spring-data/commons/docs/1.7.1.RELEASE/reference/htmlsingle/#repositories.single-repository-behaviour)), which should be recognized as _live code_ by deadcode4j for the moment
* recognize cyclic dependencies
    * actually, better to report which parts of the code are really used
    * "really used" means either marked as such manually or because the application breaks if the class (and its minions) is removed, i.e. the class is listed in sth. like web.xml
    * then we work on transitively from there
* _Detection 2.0_
    * check if a Spring bean is really used
    * check if JSF stuff is really used
    * check if GenericGenerator is really used
    * generate a report
    * consider only listed Spring Web Flow XML files
    * recognize duplicate @TypeDef?
* JBoss XML files (can be done using custom XML for the moment)
* JSP imports (with plain parsing, this means a huge effort; better look for a way to use [Jspc](http://mojo.codehaus.org/jspc-maven-plugin/) and parse the bytecode?
* Class.forName (this would probably require to analyze the .java files; also, it is most likely that this is done in a dynamic matter)

Internals
---------
* ...
