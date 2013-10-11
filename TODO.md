Features
--------
* Spring annotations
    * org.springframework.stereotype.Component
    * org.springframework.stereotype.Controller
    * org.springframework.stereotype.Service
    * org.springframework.stereotype.Repository
    * org.springframework.context.annotation.Import (hopefully this is recognized by Javassist anyways)
* JSR330 annotations
    * javax.inject.Named
* recognize cyclic dependencies
* generate a report

Internals
---------
* integrate logging; candidate for slf4j is http://code.google.com/p/slf4j-maven-plugin-log (multithreading issue though)
* use commons-io DirectoryWalker instead of plexus thing
    * also provide filter for `WEB-INF/classes` from outside, as this is nothing the DeadCodeFinder should be aware of