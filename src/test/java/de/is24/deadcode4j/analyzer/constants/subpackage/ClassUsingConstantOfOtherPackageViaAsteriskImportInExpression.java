package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.*;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantOfOtherPackageViaAsteriskImportInExpression {
    @Override
    public String toString() {
        return String.valueOf("har".equals(Constants.FOO));
    }
}
