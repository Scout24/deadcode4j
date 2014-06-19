package de.is24.deadcode4j.analyzer.constants.subpackage;
import static de.is24.deadcode4j.analyzer.constants.Constants.FOO;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantOfOtherPackageViaStaticImportInMethod {
    @Override
    public String toString() {
        return FOO;
    }
}
