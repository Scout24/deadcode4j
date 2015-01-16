package de.is24.javaparser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Fixes several bugs:
 * <ul>
 * <li>in {@link LambdaExpr}, the body and parameters aren't set as a child of the lambda expression</li>
 * <li>in {@link MethodReferenceExpr}, the scope and type parameters aren't set as a child of the method reference</li>
 * <li>in {@link TypeExpr}, the type isn't set as a child of the type expression</li>
 * <li>in {@link VoidVisitorAdapter#visit(Parameter, Object)}, the parameter's type may be <code>null</code> and thus
 * cause a <code>NullPointerException</code></li>
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
        for (Node childNode : childNodes) {
            setAsParentNode(parentNode, childNode);
        }
    }

    @Override
    public void visit(LambdaExpr n, final A arg) {
        setAsParentNode(n, n.getBody());
        setAsParentNode(n, n.getParameters());
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodReferenceExpr n, final A arg) {
        setAsParentNode(n, n.getScope());
        setAsParentNode(n, n.getTypeParameters());
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
    public void visit(TypeExpr n, final A arg) {
        setAsParentNode(n, n.getType());
        super.visit(n, arg);
    }

}
