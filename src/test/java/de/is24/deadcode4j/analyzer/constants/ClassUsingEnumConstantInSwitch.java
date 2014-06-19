package de.is24.deadcode4j.analyzer.constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingEnumConstantInSwitch {
    public void foo(EnumUsingConstantInField anEnum) {
        switch (anEnum) {
            case ENUM:
            default:
        }
    }
}
