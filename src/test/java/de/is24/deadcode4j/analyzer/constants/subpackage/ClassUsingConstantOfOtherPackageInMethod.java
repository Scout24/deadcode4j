package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.Constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantOfOtherPackageInMethod {
    @Override
    public String toString() {
        return Constants.FOO;
    }
}
