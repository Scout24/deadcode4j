package de.is24.deadcode4j.analyzer.typeerasure;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.*;
import static java.util.ResourceBundle.Control;
@SuppressWarnings("UnusedDeclaration")
public class TypedArrayList extends ArrayList<Map.Entry<java.math.BigDecimal, Comparable>> {
    private Map<Pattern, Control> someMap;
    @Override
    public String toString() {
        return new ArrayList<InnerClass>().toString();
    }
    public static class InnerClass extends ArrayList<PackageClass> {
        public static class NestedInnerClass {}
    }
    public static class SecondInnerClass {
        public ArrayList<InnerClass.NestedInnerClass> something;
    }
}