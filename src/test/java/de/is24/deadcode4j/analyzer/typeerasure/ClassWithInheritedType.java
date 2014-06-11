package de.is24.deadcode4j.analyzer.typeerasure;
import de.is24.deadcode4j.junit.SomeInterface;
import java.util.List;
@SuppressWarnings("UnusedDeclaration")
public class ClassWithInheritedType extends TypedArrayList {
    private List<InnerClass> aList;
    public static class Inner implements SomeInterface {
        private List<InnerClass.NestedInnerClass> aList;
        public static class Core {
            private List<InnerType> aList;
        }
    }
}
