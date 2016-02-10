package de.is24.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.stmt.TypeDeclarationStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import javax.annotation.Nonnull;
import java.util.Deque;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static de.is24.deadcode4j.Utils.isEmpty;

/**
 * Provides convenience methods for dealing with {@link Node}s.
 *
 * @since 2.0.0
 */
public final class Nodes {

    private Nodes() {}

    @Nonnull
    public static CompilationUnit getCompilationUnit(@Nonnull Node node) {
        Node loopNode = node;
        for (; ; ) {
            if (CompilationUnit.class.isInstance(loopNode)) {
                return CompilationUnit.class.cast(loopNode);
            }
            loopNode = loopNode.getParentNode();
            if (loopNode == null) {
                throw new RuntimeException("Couldn't locate CompilationUnit for [" + node + "]!");
            }
        }
    }

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
        Boolean typeResolved = typeDeclaration.accept(new GenericVisitorAdapter<Boolean, Void>() {
            private Deque<Integer> indexOfAnonymousClasses = newLinkedList();
            private Deque<Integer> indexOfNamedAnonymousClasses = newLinkedList();
            private int indexOfNodeToFind = anonymousClasses.size() - 1;

            @Override
            public Boolean visit(ObjectCreationExpr node, Void arg) {
                if (isEmpty(node.getAnonymousClassBody())) {
                    return super.visit(node, arg);
                }
                int currentIndex = indexOfAnonymousClasses.removeLast() + 1;
                indexOfAnonymousClasses.addLast(currentIndex);
                if (anonymousClasses.get(indexOfNodeToFind) == node) {
                    buffy.append('$').append(indexOfAnonymousClasses.getLast());
                    if (indexOfNodeToFind-- == 0) {
                        return Boolean.TRUE;
                    }
                }
                indexOfAnonymousClasses.addLast(0);
                indexOfNamedAnonymousClasses.addLast(0);
                try {
                    return super.visit(node, null);
                } finally {
                    indexOfAnonymousClasses.removeLast();
                    indexOfNamedAnonymousClasses.removeLast();
                }
            }

            @Override
            public Boolean visit(TypeDeclarationStmt node, Void arg) {
                int currentIndex = indexOfNamedAnonymousClasses.removeLast() + 1;
                indexOfNamedAnonymousClasses.addLast(currentIndex);
                if (anonymousClasses.get(indexOfNodeToFind) == node) {
                    buffy.append('$').append(indexOfNamedAnonymousClasses.getLast()).append(node.getTypeDeclaration().getName());
                    if (indexOfNodeToFind-- == 0) {
                        return Boolean.TRUE;
                    }
                }
                indexOfAnonymousClasses.addLast(0);
                indexOfNamedAnonymousClasses.addLast(0);
                try {
                    return super.visit(node, null);
                } finally {
                    indexOfAnonymousClasses.removeLast();
                    indexOfNamedAnonymousClasses.removeLast();
                }
            }

            @Override
            public Boolean visit(ClassOrInterfaceDeclaration n, Void arg) {
                indexOfAnonymousClasses.addLast(0);
                indexOfNamedAnonymousClasses.addLast(0);
                try {
                    return super.visit(n, null);
                } finally {
                    indexOfAnonymousClasses.removeLast();
                    indexOfNamedAnonymousClasses.removeLast();
                }
            }

            @Override
            public Boolean visit(EnumDeclaration n, Void arg) {
                indexOfAnonymousClasses.addLast(0);
                indexOfNamedAnonymousClasses.addLast(0);
                try {
                    return super.visit(n, null);
                } finally {
                    indexOfAnonymousClasses.removeLast();
                    indexOfNamedAnonymousClasses.removeLast();
                }
            }

        }, null);
        assert typeResolved : "Failed to locate anonymous class definition!";
        anonymousClasses.clear();
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
