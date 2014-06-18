Features
--------
* [x] @dcsobral: possibility to ignore all classes with main method
* [ ] recognize cyclic dependencies
    * [ ] actually, better to report which parts of the code are really used
    * [ ] "really used" means either marked as such manually or because the application breaks if the class (and its minions) is removed, i.e. the class is listed in sth. like web.xml
    * [ ] then we work on transitively from there
* [ ] _Detection 2.0_
    * [ ] check if a Spring bean is really used
    * [ ] check if JSF stuff is really used
    * [x] check if GenericGenerator is really used
    * [ ] generate a report
    * [ ] consider only listed Spring Web Flow XML files
* [ ] JBoss XML files (can be done using custom XML for the moment)
* [ ] JSP imports (with plain parsing, this means a huge effort; better look for a way to use [Jspc](http://mojo.codehaus.org/jspc-maven-plugin/) and parse the bytecode?
* [ ] Class.forName (this would probably require to analyze the .java files; also, it is most likely that this is done in a dynamic matter)

Internals
---------
* [ ] see if JDK's [`jdeps`](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8003562) can be used instead of the custom dependency detection
