package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.Expression;
import dev.turtywurty.pepolang.parser.ExpressionVisitor;
import dev.turtywurty.pepolang.parser.Statement;
import dev.turtywurty.pepolang.parser.StatementVisitor;

import java.util.Objects;

public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Void> {
    public void interpret(Expression expression) {
        try {
            Object value = evaluate(expression);
            System.out.println("Result: " + stringify(value));
        } catch (RuntimeError error) {
            throw new RuntimeException("Something went wrong in the interpreter!", error);
        }
    }

    private String stringify(Object object) {
        if (object == null) return "null";
        return object.toString();
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
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.PrintStatement statement) {
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
