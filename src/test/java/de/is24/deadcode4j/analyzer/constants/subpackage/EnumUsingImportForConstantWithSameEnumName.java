package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.Constants;
@SuppressWarnings({"UnusedDeclaration"})
public enum EnumUsingImportForConstantWithSameEnumName {
    Constants,
    Other;
    public final String FOO = "bar";
    private static class InnerClass {
        public final String foo = Constants.FOO;
    }
}
