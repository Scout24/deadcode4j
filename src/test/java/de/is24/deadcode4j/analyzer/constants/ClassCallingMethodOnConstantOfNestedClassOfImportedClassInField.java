package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassCallingMethodOnConstantOfNestedClassOfImportedClassInField {
    private String foo = Constants.More.STUFF.intern();
}