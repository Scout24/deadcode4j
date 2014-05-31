package de.is24.deadcode4j.analyzer.typeerasure;
import de.is24.deadcode4j.junit.SomeInterface;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;

@SuppressWarnings("UnusedDeclaration")
public class ClassWithAnonymousClasses {
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            new ArrayList<String>();
            new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return null;
                }
            };
            new Object() {
                @Override
                public String toString() {
                    new ArrayList<SomeInterface>();
                    return super.toString();
                }
            };
            class AnonymousInner {
                private ArrayList<String> aList;
            }
        }
    };
    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            new ArrayList<Set>();
        }
    };
}
