package de.is24.deadcode4j.analyzer.constants.subpackage;
import de.is24.deadcode4j.analyzer.constants.Constants;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingInnerClassOfConstantOfOtherPackageInMethodCall {
    @SuppressWarnings("RedundantStringToString")
    public final String foo = Constants.More.STUFF.toString();
}
