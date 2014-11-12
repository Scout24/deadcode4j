package de.is24.javaparser;
public class TypeNameTestClass {
    String s = "de.is24.javaparser.TypeNameTestClass";
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            "de.is24.javaparser.TypeNameTestClass$1".toString();
            new Runnable() {
                @Override
                public void run() {
                    "de.is24.javaparser.TypeNameTestClass$1$1".toString();
                }
            };
            class AnonymousInner {
                String s = "de.is24.javaparser.TypeNameTestClass$1$1AnonymousInner";
            }
            new Object() {
                String s = "de.is24.javaparser.TypeNameTestClass$1$2";
            };
        }
    };
    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            "de.is24.javaparser.TypeNameTestClass$2".toString();
        }
    };
    private Object object = new Object() {
        String s = "de.is24.javaparser.TypeNameTestClass$3";
    };
    private static class InnerClass {
        String s = "de.is24.javaparser.TypeNameTestClass$InnerClass";
        private Object object = new Object() {
            String s = "de.is24.javaparser.TypeNameTestClass$InnerClass$1";
            private void foo() {
                class AnonymousInnerInAnonymous {
                    String s = "de.is24.javaparser.TypeNameTestClass$InnerClass$1$1AnonymousInnerInAnonymous";
                }
            }
        };
        public void foo() {
            class FirstAnonymousInner {
                String s = "de.is24.javaparser.TypeNameTestClass$InnerClass$1FirstAnonymousInner";
                private Object object1 = new Object() {
                    String s = "de.is24.javaparser.TypeNameTestClass$InnerClass$1FirstAnonymousInner$1";
                };
                private Object object2 = new Object() {
                    String s = "de.is24.javaparser.TypeNameTestClass$InnerClass$1FirstAnonymousInner$2";
                };
            }
        }
        public void bar() {
            class SecondAnonymousInner {
                String s = "de.is24.javaparser.TypeNameTestClass$InnerClass$2SecondAnonymousInner";
            }
        }
    }
    private Object secondObject = new Object() {
        String s = "de.is24.javaparser.TypeNameTestClass$4";
    };
    private static enum Count {
        ONE, TWO, THREE;
        String s = "de.is24.javaparser.TypeNameTestClass$Count";
        Object object = new Object() {
            String s = "de.is24.javaparser.TypeNameTestClass$Count$1";
        };
    }
    private Object thirdObject = new Object() {
        String s = "de.is24.javaparser.TypeNameTestClass$5";
    };
}
