Features
--------
* recognize `javax.servlet.ServletContainerInitializer`s and `org.springframework.web.WebApplicationInitializer`s as live code (in case of a war artifact?)
* recognize [custom Spring Data repositories](http://docs.spring.io/spring-data/data-commons/docs/1.6.x/reference/html/repositories.html#repositories.custom-implementations)
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
