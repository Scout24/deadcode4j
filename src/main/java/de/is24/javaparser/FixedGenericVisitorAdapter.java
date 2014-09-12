package de.is24.javaparser;

import de.is24.deadcode4j.Utils;
import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.visitor.GenericVisitorAdapter;

/**
 * Fixes several bugs:
 * <ul>
 *     <li>in {@link japa.parser.ast.body.AnnotationMemberDeclaration}, the default value expression isn't set as a
 *     child of the declaration</li>
 *     <li>in {@link japa.parser.ast.stmt.TryStmt}, the variable declarations aren't set as a child of the try
 *     statement</li>
 *     <li>in {@link japa.parser.ast.visitor.GenericVisitorAdapter#visit(japa.parser.ast.stmt.TryStmt, Object)},
 *     the variable declarations aren't visited</li>
 * </ul>
 *
 * @since 2.0.0
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

    @Override
    public R visit(TryStmt n, A arg) {
        for (VariableDeclarationExpr variableDeclarationExpr : Utils.emptyIfNull(n.getResources())) {
            variableDeclarationExpr.setParentNode(n);
            R result = variableDeclarationExpr.accept(this, arg);
            if (result != null) {
                return result;
            }
        }
        return super.visit(n, arg);
    }

}
