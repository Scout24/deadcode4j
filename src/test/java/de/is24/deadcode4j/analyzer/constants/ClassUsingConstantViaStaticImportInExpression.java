package de.is24.deadcode4j.analyzer.constants;
import static de.is24.deadcode4j.analyzer.constants.Constants.FOO;
import static java.lang.String.valueOf;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantViaStaticImportInExpression {
    @Override
    public String toString() {
        return valueOf("har".equals(FOO));
    }
}
