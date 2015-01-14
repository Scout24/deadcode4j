package de.is24.javaparser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import de.is24.deadcode4j.Utils;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

/**
 * Fixes several bugs:
 * <ul>
 *     <li>in {@link AnnotationMemberDeclaration}, the default value expression isn't set as a child of the
 *     declaration</li>
 *     <li>in {@link LambdaExpr}, the body and parameters aren't set as a child of the lambda expression</li>
 *     <li>in {@link TryStmt}, the variable declarations aren't set as a child of the try statement</li>
 *     <li>in {@link GenericVisitorAdapter#visit(Parameter, Object)}, the parameter's type may be <code>null</code> and
 *     thus cause a <code>NullPointerException</code></li>
 *     <li>in {@link GenericVisitorAdapter#visit(TryStmt, Object)}, the variable declarations aren't visited</li>
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
        for (Node childNode: childNodes) {
            setAsParentNode(parentNode, childNode);
        }
    }

    @Override
    public R visit(AnnotationMemberDeclaration n, A arg) {
        Expression defaultValue = n.getDefaultValue();
        if (defaultValue != null) {
            defaultValue.setParentNode(n);
        }
        return super.visit(n, arg);
    }

    @Override
    public R visit(LambdaExpr n, A arg) {
        setAsParentNode(n, n.getBody());
        setAsParentNode(n, n.getParameters());
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
