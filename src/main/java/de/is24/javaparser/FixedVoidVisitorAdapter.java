package de.is24.javaparser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import de.is24.deadcode4j.Utils;

/**
 * Fixes several bugs:
 * <ul>
 * <li>in {@link AnnotationMemberDeclaration}, the default value expression isn't set as a child of the declaration</li>
 * <li>in {@link LambdaExpr}, the body and parameters aren't set as a child of the lambda expression</li>
 * <li>in {@link TryStmt}, the variable declarations aren't set as a child of the try statement</li>
 * <li>in {@link VoidVisitorAdapter#visit(Parameter, Object)}, the parameter's type may be <code>null</code> and thus
 * cause a <code>NullPointerException</code></li>
 * <li>in {@link VoidVisitorAdapter#visit(TryStmt, Object)}, the variable declarations aren't visited</li>
 * </ul>
 *
 * @since 2.0.0
 */
public class FixedVoidVisitorAdapter<A> extends VoidVisitorAdapter<A> {

    private void visitComment(final Comment n, final A arg) {
        if (n != null) {
            n.accept(this, arg);
        }
    }

    private void setAsParentNode(Node parentNode, Node childNode) {
        if (childNode != null) {
            childNode.setParentNode(parentNode);
        }
    }

    private void setAsParentNode(Node parentNode, Iterable<? extends Node> childNodes) {
        if (childNodes == null) {
            return;
        }
        for (Node childNode: childNodes) {
            setAsParentNode(parentNode, childNode);
        }
    }

    @Override
    public void visit(AnnotationMemberDeclaration n, A arg) {
        Expression defaultValue = n.getDefaultValue();
        if (defaultValue != null) {
            defaultValue.setParentNode(n);
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(LambdaExpr n, final A arg) {
        setAsParentNode(n, n.getBody());
        setAsParentNode(n, n.getParameters());
        super.visit(n, arg);
    }

    @Override
    public void visit(final Parameter n, final A arg) {
        visitComment(n.getComment(), arg);
        if (n.getAnnotations() != null) {
            for (final AnnotationExpr a : n.getAnnotations()) {
                a.accept(this, arg);
            }
        }
        if (n.getType() != null) {
            n.getType().accept(this, arg);
        }
        n.getId().accept(this, arg);
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
