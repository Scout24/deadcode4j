package de.is24.javaparser;

import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.visitor.VoidVisitorAdapter;

/**
 * Fixes a bug in {@link japa.parser.ast.body.AnnotationMemberDeclaration} where the default value expression isn't set
 * as a child of the declaration.
 *
 * @since 1.6
 */
public class FixedVoidVisitorAdapter<A> extends VoidVisitorAdapter<A> {

    @Override
    public void visit(AnnotationMemberDeclaration n, A arg) {
        Expression defaultValue = n.getDefaultValue();
        if (defaultValue != null) {
            defaultValue.setParentNode(n);
        }
        super.visit(n, arg);
    }

}
