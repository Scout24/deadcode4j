package de.is24.javaparser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

/**
 * Fixes several bugs:
 * <ul>
 * <li>in {@link LambdaExpr}, the body and parameters aren't set as a child of the lambda expression</li>
 * <li>in {@link MethodReferenceExpr}, the scope and type parameters aren't set as a child of the method reference</li>
 * <li>in {@link TypeExpr}, the type isn't set as a child of the type expression</li>
 * <li>in {@link GenericVisitorAdapter#visit(Parameter, Object)}, the parameter's type may be <code>null</code> and thus
 * cause a <code>NullPointerException</code></li>
 * </ul>
 *
 * @since 2.0.0
 */
public class FixedGenericVisitorAdapter<R, A> extends GenericVisitorAdapter<R, A> {

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
    public R visit(LambdaExpr n, A arg) {
        setAsParentNode(n, n.getBody());
        setAsParentNode(n, n.getParameters());
        return super.visit(n, arg);
    }

    @Override
    public R visit(MethodReferenceExpr n, A arg) {
        setAsParentNode(n, n.getScope());
        setAsParentNode(n, n.getTypeParameters());
        return super.visit(n, arg);
    }

    @Override
    public R visit(final Parameter n, final A arg) {
        if (n.getAnnotations() != null) {
            for (final AnnotationExpr a : n.getAnnotations()) {
                {
                    R result = a.accept(this, arg);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        if (n.getType() != null) {
            R result = n.getType().accept(this, arg);
            if (result != null) {
                return result;
            }
        }
        {
            R result = n.getId().accept(this, arg);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public R visit(TypeExpr n, A arg) {
        setAsParentNode(n, n.getType());
        return super.visit(n, arg);
    }

}
