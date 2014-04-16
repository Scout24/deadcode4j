package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.Constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantOfOtherPackageInExpression {
    @Override
    public String toString() {
        return String.valueOf( "har".equals(Constants.FOO));
    }
}
