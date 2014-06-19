package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassCallingMethodOnConstantOfImportedClassInField {
    private String foo = Constants.FOO.intern();
}