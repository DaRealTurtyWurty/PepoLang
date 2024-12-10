package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;

import java.util.List;

public class AstPrinter implements ExpressionVisitor<String>, StatementVisitor<Void> {
    public static void main(String[] args) {
        var expression = new Expression.Binary(
                new Expression.Unary(
                        new Token(TokenType.SUB, "-", 1),
                        new Expression.Literal(123)
                ),
                new Token(TokenType.MUL, "*", 1),
                new Expression.Grouping(
                        new Expression.Literal(45.67)
                )
        );

        System.out.println(AstPrinter.print(expression));
    }

    public static String print(Expression expr) {
        return expr.accept(new AstPrinter());
    }

    public static String print(List<Statement> statements) {
        var printer = new AstPrinter();
        var builder = new StringBuilder();

        for (Statement statement : statements) {
            builder.append(statement.accept(printer)).append("\n");
        }

        return builder.toString();
    }

    @Override
    public String visitGrouping(Expression.Grouping expression) {
        return parenthesize("group", expression.getExpression());
    }

    @Override
    public String visitLiteral(Expression.Literal expression) {
        return expression.getValue() == null ? "null" : expression.getValue().toString();
    }

    @Override
    public String visitUnary(Expression.Unary expression) {
        return parenthesize(expression.getOperator().type().name(), expression.getRight());
    }

    @Override
    public String visitVariable(Expression.Variable expression) {
        return (String) expression.getName().value();
    }

    @Override
    public String visitBinary(Expression.Binary expression) {
        return parenthesize(expression.getOperator().type().name(), expression.getLeft(), expression.getRight());
    }

    private String parenthesize(String name, Expression... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression expr : expressions) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.PrintStatement statement) {
        return null;
    }

    @Override
    public Void visitVariableStatement(Statement.VariableStatement statement) {
        return null;
    }
}
