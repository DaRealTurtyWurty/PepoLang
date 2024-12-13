package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.*;

import java.util.List;
import java.util.Objects;

public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Void> {
    private final Environment environment = new Environment();

    public void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            throw new RuntimeException("Something went wrong in the interpreter!", error);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) return "null";
        return object.toString();
    }

    @Override
    public Object visitAssign(Expression.Assign expression) {
        Object value = evaluate(expression.getValue());
        environment.assign(expression.getName(), value);
        return value;
    }

    @Override
    public Object visitBinary(Expression.Binary expression) {
        Object left = evaluate(expression.getLeft());
        Object right = evaluate(expression.getRight());

        switch (expression.getOperator().type()) {
            case SUB -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() - rightNum.doubleValue();

                return null;
            }
            case ADD -> {
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() + rightNum.doubleValue();

                if (left instanceof String leftStr && right instanceof String rightStr)
                    return leftStr + rightStr;

                throw new RuntimeError(expression.getOperator(), "Operands must be two numbers or two strings.");
            }
            case MUL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() * rightNum.doubleValue();

                return null;
            }
            case DIV -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum) {
                    if(rightNum.doubleValue() == 0)
                        throw new RuntimeError(expression.getOperator(), "Cannot divide by zero!");

                    return leftNum.doubleValue() / rightNum.doubleValue();
                }

                return null;
            }
            case GT -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() > rightNum.doubleValue();

                return null;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() >= rightNum.doubleValue();

                return null;
            }
            case LT -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() < rightNum.doubleValue();

                return null;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() <= rightNum.doubleValue();

                return null;
            }
            case EQUAL -> {
                if (left == null && right == null)
                    return true;

                if (left == null)
                    return false;

                return Objects.equals(left, right);
            }
            case NOT_EQUAL -> {
                if (left == null && right == null)
                    return false;

                if (left == null)
                    return true;

                return !Objects.equals(left, right);
            }
        }

        return null; // Unreachable
    }

    @Override
    public Object visitGrouping(Expression.Grouping expression) {
        return evaluate(expression.getExpression());
    }

    @Override
    public Object visitLiteral(Expression.Literal expression) {
        return expression.getValue();
    }

    @Override
    public Object visitUnary(Expression.Unary expression) {
        Object right = evaluate(expression.getRight());

        return switch (expression.getOperator().type()) {
            case SUB -> -(double) right;
            case ADD -> right;
            case NOT -> !isTruthy(right);
            default -> null;
        };
    }

    @Override
    public Object visitVariable(Expression.Variable expression) {
        return environment.get((String) expression.getName().value());
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Number) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Number && right instanceof Number) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;

        if (object instanceof Boolean bool)
            return bool;

        return true;
    }

    public Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
        evaluate(statement.getExpression());
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.PrintStatement statement) {
        Object value = evaluate(statement.getExpression());
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVariableStatement(Statement.VariableStatement statement) {
        Object value = null;
        if (statement.getInitializer() != null) {
            value = evaluate(statement.getInitializer());
        }

        environment.define((String) statement.getName().value(), value); // TODO: Handle types
        return null;
    }

    public static class RuntimeError extends RuntimeException {
        final Token token;

        RuntimeError(Token token, String message) {
            super(message);
            this.token = token;
        }
    }
}
