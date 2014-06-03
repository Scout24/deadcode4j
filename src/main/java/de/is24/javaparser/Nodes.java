package de.is24.javaparser;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.stmt.TypeDeclarationStmt;

import javax.annotation.Nonnull;
import java.util.Deque;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static de.is24.deadcode4j.Utils.isEmpty;
import static java.util.Collections.singleton;

/**
 * Provides convenience methods for dealing with {@link japa.parser.ast.Node}s.
 *
 * @since 1.6
 */
public class Nodes {

    @Nonnull
    public static String getTypeName(@Nonnull Node node) {
        List<Node> anonymousClasses = newArrayList();
        StringBuilder buffy = new StringBuilder();
        Node loopNode = node;
        for (; ; ) {
            if (ObjectCreationExpr.class.isInstance(loopNode)) {
                if (!isEmpty(ObjectCreationExpr.class.cast(loopNode).getAnonymousClassBody())) {
                    anonymousClasses.add(loopNode);
                }
            } else if (TypeDeclarationStmt.class.isInstance(loopNode)) {
                anonymousClasses.add(loopNode);
            } else if (TypeDeclaration.class.isInstance(loopNode)
                    && !TypeDeclarationStmt.class.isInstance(loopNode.getParentNode())) {
                TypeDeclaration typeDeclaration = TypeDeclaration.class.cast(loopNode);
                prependSeparatorIfNecessary('$', buffy).insert(0, typeDeclaration.getName());
                appendAnonymousClasses(anonymousClasses, typeDeclaration, buffy);
            } else if (CompilationUnit.class.isInstance(loopNode)) {
                if (buffy.length() == 0) {
                    buffy.append("package-info");
                }
                final CompilationUnit compilationUnit = CompilationUnit.class.cast(loopNode);
                if (compilationUnit.getPackage() != null) {
                    prepend(compilationUnit.getPackage().getName(), buffy);
                }
            }
            loopNode = loopNode.getParentNode();
            if (loopNode == null) {
                return buffy.toString();
            }
        }
    }

    private static void appendAnonymousClasses(@Nonnull final List<Node> anonymousClasses,
                                               @Nonnull TypeDeclaration typeDeclaration,
                                               @Nonnull final StringBuilder buffy) {
        if (anonymousClasses.isEmpty()) {
            return;
        }
        Boolean typeResolved = typeDeclaration.accept(new FixedGenericVisitorAdapter<Boolean, Void>() {
            private Deque<Integer> indexOfAnonymousClasses = newLinkedList(singleton(0));
            private int indexOfNodeToFind = anonymousClasses.size() - 1;

            @Override
            public Boolean visit(ObjectCreationExpr node, Void arg) {
                if (isEmpty(node.getAnonymousClassBody())) {
                    return super.visit(node, arg);
                }
                int currentIndex = indexOfAnonymousClasses.removeLast() + 1;
                indexOfAnonymousClasses.addLast(currentIndex);
                if (anonymousClasses.get(indexOfNodeToFind) == node) {
                    appendTypeName("");
                    if (indexOfNodeToFind-- == 0) {
                        return Boolean.TRUE;
                    }
                }
                indexOfAnonymousClasses.addLast(0);
                try {
                    return super.visit(node, null);
                } finally {
                    indexOfAnonymousClasses.removeLast();
                }
            }

            @Override
            public Boolean visit(TypeDeclarationStmt node, Void arg) {
                int currentIndex = indexOfAnonymousClasses.removeLast() + 1;
                indexOfAnonymousClasses.addLast(currentIndex);
                if (anonymousClasses.get(indexOfNodeToFind) == node) {
                    appendTypeName(node.getTypeDeclaration().getName());
                    if (indexOfNodeToFind-- == 0) {
                        return Boolean.TRUE;
                    }
                }
                indexOfAnonymousClasses.addLast(0);
                try {
                    return super.visit(node, null);
                } finally {
                    indexOfAnonymousClasses.removeLast();
                }
            }

            private void appendTypeName(String typeSuffix) {
                buffy.append('$').append(indexOfAnonymousClasses.getLast()).append(typeSuffix);
            }

        }, null);
        assert typeResolved : "Failed to locate anonymous class definition!";
    }

    @Nonnull
    public static StringBuilder prepend(@Nonnull NameExpr nameExpr, @Nonnull StringBuilder buffy) {
        for (; ; ) {
            prependSeparatorIfNecessary('.', buffy).insert(0, nameExpr.getName());
            if (!QualifiedNameExpr.class.isInstance(nameExpr)) {
                return buffy;
            }
            nameExpr = QualifiedNameExpr.class.cast(nameExpr).getQualifier();
        }
    }

    @Nonnull
    public static StringBuilder prependSeparatorIfNecessary(char character, @Nonnull StringBuilder buffy) {
        if (buffy.length() > 0) {
            buffy.insert(0, character);
        }
        return buffy;
    }

}
