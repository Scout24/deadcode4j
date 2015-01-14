package de.is24.javaparser;

import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import de.is24.deadcode4j.Utils;

/**
 * Fixes several bugs:
 * <ul>
 *     <li>in {@link AnnotationMemberDeclaration}, the default value expression isn't set as a
 *     child of the declaration</li>
 *     <li>in {@link TryStmt}, the variable declarations aren't set as a child of the try
 *     statement</li>
 *     <li>in {@link VoidVisitorAdapter#visit(TryStmt, Object)},
 *     the variable declarations aren't visited</li>
 * </ul>
 *
 * @since 2.0.0
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
