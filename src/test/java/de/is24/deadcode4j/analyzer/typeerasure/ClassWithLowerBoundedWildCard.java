package de.is24.deadcode4j.analyzer.typeerasure;
import java.util.ArrayList;
import java.util.Collection;
@SuppressWarnings("UnusedDeclaration")
public class ClassWithLowerBoundedWildCard {
    private ArrayList<? super Collection> aList;
}
