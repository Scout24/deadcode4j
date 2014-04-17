package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.*;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantOfOtherPackageViaAsteriskImportInMethod {
    @Override
    public String toString() {
        return Constants.FOO;
    }
}
