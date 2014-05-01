package de.is24.deadcode4j.analyzer.typeerasure;
import java.util.ArrayList;
import java.util.Map;
@SuppressWarnings("UnusedDeclaration")
public class TypedArrayList extends ArrayList<Map.Entry<java.math.BigDecimal, Comparable>> {
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