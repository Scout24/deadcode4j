package de.is24.deadcode4j.analyzer.customrepositories;
import org.springframework.data.repository.Repository;
@SuppressWarnings("UnusedDeclaration")
public interface FooRepository extends Repository<Foo, Long>, FooRepositoryCustom { }
