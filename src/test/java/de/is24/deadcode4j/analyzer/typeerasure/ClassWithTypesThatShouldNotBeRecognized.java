package de.is24.deadcode4j.analyzer.typeerasure;
import java.util.ArrayList;
@SuppressWarnings("UnusedDeclaration")
public class ClassWithTypesThatShouldNotBeRecognized {
    public ClassWithTypesThatShouldNotBeRecognized() {
            new ArrayList<long[]>();
            ArrayList<?> aList;
    }
}
