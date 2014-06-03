package de.is24.javaparser;

import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class A_Nodes {

    @Test
    public void prependsNothingForEmptyStringBuilder() {
        StringBuilder buffy = Nodes.prependSeparatorIfNecessary('.', new StringBuilder());

        assertThat(buffy.toString(), is(""));
    }

    @Test
    public void prependsSeparator() {
        StringBuilder buffy = Nodes.prependSeparatorIfNecessary('.', new StringBuilder("foo"));

        assertThat(buffy.toString(), is(".foo"));
    }

    @Test
    public void prependsNameExpression() {
        StringBuilder buffy = Nodes.prepend(new NameExpr("foo"), new StringBuilder("Bar"));

        assertThat(buffy.toString(), is("foo.Bar"));
    }

    @Test
    public void prependsQualifiedNameExpression() {
        QualifiedNameExpr qualifiedNameExpr =
                new QualifiedNameExpr(new QualifiedNameExpr(new NameExpr("de"), "is24"), "foo");
        StringBuilder buffy = Nodes.prepend(qualifiedNameExpr, new StringBuilder("Bar"));

        assertThat(buffy.toString(), is("de.is24.foo.Bar"));
    }

}
