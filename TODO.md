Features
--------
* Spring annotations
    * org.springframework.stereotype.Component
    * org.springframework.stereotype.Controller
    * org.springframework.stereotype.Service
    * org.springframework.stereotype.Repository
    * org.springframework.context.annotation.Import (hopefully this is recognized by Javassist anyways)
* JEE6
    * javax.inject.Named
    * javax.persistence.metamodel.StaticMetamodel
* Hibernate annotations
    * org.hibernate.annotations.Type
    * investigate
* recognize cyclic dependencies
* generate a report

Internals
---------
* integrate logging; candidate for slf4j is http://code.google.com/p/slf4j-maven-plugin-log (multithreading issue though)