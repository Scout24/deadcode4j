package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import japa.parser.JavaParser;
import japa.parser.ast.*;
import japa.parser.ast.body.*;
import japa.parser.ast.comments.BlockComment;
import japa.parser.ast.comments.JavadocComment;
import japa.parser.ast.comments.LineComment;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.*;
import japa.parser.ast.type.*;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import javax.annotation.Nonnull;
import java.io.File;

public class ReferenceToConstantsAnalyzer extends AnalyzerAdapter {

    @Override
    public void doAnalysis(@Nonnull CodeContext codeContext, @Nonnull File file) {
        if (file.getName().endsWith(".java")) {
            logger.debug("Analyzing Java file [{}]...", file);
            analyzeJavaFile(codeContext, file);
        }
    }

    private void analyzeJavaFile(CodeContext codeContext, File file) {
        CompilationUnit compilationUnit;
        try {
            compilationUnit = JavaParser.parse(file, null, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse [" + file + "]!", e);
        }

        System.out.println(compilationUnit);
        compilationUnit.accept(new VoidVisitorAdapter<Void>() {

            int depth = 0;

            @Override
            public void visit(AnnotationDeclaration n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(AnnotationMemberDeclaration n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ArrayAccessExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ArrayCreationExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ArrayInitializerExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(AssertStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(BinaryExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(BlockComment n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(BlockStmt n, Void arg) {
                print(n, n.getStmts());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(BooleanLiteralExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(BreakStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(CastExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(CatchClause n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(CharLiteralExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ClassExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                print(n, n.getName());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(ClassOrInterfaceType n, Void arg) {
                print(n, n.getName() + "/" + n.getScope() + "/" + n.getTypeArgs());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(CompilationUnit n, Void arg) {
                print(n, "");
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(ConditionalExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ConstructorDeclaration n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ContinueStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(DoStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(DoubleLiteralExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(EmptyMemberDeclaration n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(EmptyStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(EmptyTypeDeclaration n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(EnclosedExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(EnumConstantDeclaration n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(EnumDeclaration n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ExplicitConstructorInvocationStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ExpressionStmt n, Void arg) {
                print(n, n.getExpression());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(FieldAccessExpr n, Void arg) {
                print(n, n.getScope() + "." + n.getField() + "/" + n.getFieldExpr() + "/" +  n.getTypeArgs());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(FieldDeclaration n, Void arg) {
                print(n, n.getType() + "/" + n.getVariables());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(ForeachStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ForStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(IfStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ImportDeclaration n, Void arg) {
                print(n, n.getName() + "/static: " + n.isStatic() + "/asterisk: " + n.isAsterisk());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(InitializerDeclaration n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(InstanceOfExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(IntegerLiteralExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(IntegerLiteralMinValueExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(JavadocComment n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(LabeledStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(LineComment n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(LongLiteralExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(LongLiteralMinValueExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(MarkerAnnotationExpr n, Void arg) {
                print(n, n.getName());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(MemberValuePair n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(MethodCallExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(MethodDeclaration n, Void arg) {
                print(n, n.getName() + ":" + n.getType());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(NameExpr n, Void arg) {
                print(n, n.getName());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(NormalAnnotationExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(NullLiteralExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ObjectCreationExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(PackageDeclaration n, Void arg) {
                print(n, n.getName() + "/" + n.getAnnotations());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(Parameter n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(MultiTypeParameter n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(PrimitiveType n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(QualifiedNameExpr n, Void arg) {
                print(n,  n.getQualifier() + "/" + n.getName() );
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(ReferenceType n, Void arg) {
                print(n, n.getType());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(ReturnStmt n, Void arg) {
                print(n, n.getExpr());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(SingleMemberAnnotationExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(StringLiteralExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(SuperExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(SwitchEntryStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(SwitchStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(SynchronizedStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ThisExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(ThrowStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(TryStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(TypeDeclarationStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(TypeParameter n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(UnaryExpr n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(VariableDeclarationExpr n, Void arg) {
                print(n, n.getVars() + ":" + n.getType());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(VariableDeclarator n, Void arg) {
                print(n, n.getId() + "/" + n.getInit());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(VariableDeclaratorId n, Void arg) {
                print(n, n.getName() + "/" + n.getArrayCount());
                depth++;
                super.visit(n, arg);
                depth--;
            }

            @Override
            public void visit(VoidType n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(WhileStmt n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(WildcardType n, Void arg) {
                print(n, null);
            }

            @Override
            public void visit(AssignExpr n, Void arg) {
                print(n, n.getValue());
            }

            private void print(Node node, Object content) {
                StringBuilder buffy = new StringBuilder(16);
                for (int blanks = depth * 2; blanks-- > 0;) {
                    buffy.append(" ");
                }
                System.out.println(buffy + node.getClass().getSimpleName() + " [" + content + "]@"
                        + node.getBeginLine() + "." + node.getBeginColumn() + ": " + node.getData());
            }
        }, null);
    }

}
