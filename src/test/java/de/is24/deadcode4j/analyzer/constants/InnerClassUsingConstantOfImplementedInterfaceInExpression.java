package de.is24.deadcode4j.analyzer.constants;
import static java.lang.String.valueOf;
@SuppressWarnings("UnusedDeclaration")
public class InnerClassUsingConstantOfImplementedInterfaceInExpression implements Constants {
    public static class InnerClass {
        @Override
        public String toString() {
            return valueOf("har".equals(FOO));
        }
    }
}
