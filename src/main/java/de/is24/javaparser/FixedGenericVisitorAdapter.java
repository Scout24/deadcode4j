package de.is24.javaparser;

import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.visitor.GenericVisitorAdapter;

/**
 * Fixes a bug in {@link japa.parser.ast.body.AnnotationMemberDeclaration} where the default value expression wasn't set
 * as a child of the declaration.
 *
 * @since 1.6
 */
public class FixedGenericVisitorAdapter<R, A> extends GenericVisitorAdapter<R, A> {

    @Override
    public R visit(AnnotationMemberDeclaration n, A arg) {
        Expression defaultValue = n.getDefaultValue();
        if (defaultValue != null) {
            defaultValue.setParentNode(n);
        }
        return super.visit(n, arg);
    }

}
