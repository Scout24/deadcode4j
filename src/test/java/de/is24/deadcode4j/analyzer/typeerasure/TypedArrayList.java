package de.is24.deadcode4j.analyzer.typeerasure;
import java.util.ArrayList;
@SuppressWarnings("UnusedDeclaration")
public class TypedArrayList extends ArrayList<Comparable<java.math.BigDecimal>> {
    @Override
    public String toString() {
        return new ArrayList<InnerClass>().toString();
    }
    public static class InnerClass {
        public static class NestedInnerClass {}
    }
    public static class SecondInnerClass {
        public ArrayList<InnerClass.NestedInnerClass> something;
    }
}