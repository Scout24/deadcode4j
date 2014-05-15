package de.is24.deadcode4j.analyzer.customrepositories;
@SuppressWarnings("UnusedDeclaration")
public class FooRepositoryImpl implements FooRepositoryCustom {
    @Override
    public Foo findByObject(Object o) {
        return new Foo();
    }
}
