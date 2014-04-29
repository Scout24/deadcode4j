package de.is24.deadcode4j.analyzer.constants;

@SuppressWarnings("UnusedDeclaration")
public class ClassUsingStaticImportForConstantWithSameFieldNameBeingDeclaredAfterItIsReferencedInMethod {
    @Override
    public String toString() {
        return FOO;
    }
    private final String FOO = "test";
}
