package de.is24.javaparser;

import de.is24.deadcode4j.Utils;
import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;

/**
 * Fixes several bugs:
 * <ul>
 *     <li>in {@link japa.parser.ast.body.AnnotationMemberDeclaration}, the default value expression isn't set as a
 *     child of the declaration</li>
 *     <li>in {@link japa.parser.ast.stmt.TryStmt}, the variable declarations aren't set as a child of the try
 *     statement</li>
 *     <li>in {@link japa.parser.ast.visitor.VoidVisitorAdapter#visit(japa.parser.ast.stmt.TryStmt, Object)},
 *     the variable declarations aren't visited</li>
 * </ul>
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

    @Override
    public void visit(TryStmt n, A arg) {
        for (VariableDeclarationExpr variableDeclarationExpr : Utils.emptyIfNull(n.getResources())) {
            variableDeclarationExpr.setParentNode(n);
            variableDeclarationExpr.accept(this, arg);
        }
        super.visit(n, arg);
    }

}
