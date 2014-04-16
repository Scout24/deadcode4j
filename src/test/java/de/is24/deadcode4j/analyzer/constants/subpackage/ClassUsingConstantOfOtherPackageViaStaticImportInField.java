package de.is24.deadcode4j.analyzer.constants.subpackage;
import static de.is24.deadcode4j.analyzer.constants.Constants.FOO;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantOfOtherPackageViaStaticImportInField {
    public final String foo = FOO;
}
