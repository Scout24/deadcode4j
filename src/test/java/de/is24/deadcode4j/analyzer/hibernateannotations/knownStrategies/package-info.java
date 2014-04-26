@GenericGenerators({
        @GenericGenerator(name = "generatorOne", strategy = "IndependentClass"),
        @GenericGenerator(name = "generatorTwo", strategy = "uuid"),
        @GenericGenerator(name = "generatorThree", strategy = "DependingClass")
})
@TypeDefs({
        @TypeDef(name = "byteClass", typeClass = Byte.class),
        @TypeDef(name = "shortClass", typeClass = Short.class)}) package de.is24.deadcode4j.analyzer.hibernateannotations.knownStrategies;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.GenericGenerators;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;