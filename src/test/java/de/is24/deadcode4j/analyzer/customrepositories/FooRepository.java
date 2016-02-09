package de.is24.deadcode4j.analyzer.customrepositories;
import org.springframework.data.repository.CrudRepository;
@SuppressWarnings("UnusedDeclaration")
public interface FooRepository extends CrudRepository<Foo, Long>, FooRepositoryCustom { }
