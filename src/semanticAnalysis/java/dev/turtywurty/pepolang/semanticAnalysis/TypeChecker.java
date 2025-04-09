package dev.turtywurty.pepolang.semanticAnalysis;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;

import java.util.HashMap;
import java.util.Map;

public class TypeChecker {
    private static final Map<TokenType, BiMap<PrimitiveType, PrimitiveType, PrimitiveType>> BINARY_OPERATOR_RULES = new HashMap<>();

    static {
        PrimitiveType[] numericTypeOrder = {
                PrimitiveType.DOUBLE,
                PrimitiveType.FLOAT,
                PrimitiveType.LONG,
                PrimitiveType.INT,
                PrimitiveType.SHORT,
                PrimitiveType.BYTE
        };

        for (int resultIndex = 0; resultIndex < numericTypeOrder.length; resultIndex++) {
            PrimitiveType type = numericTypeOrder[resultIndex];
            for (int leftIndex = 0; leftIndex <= resultIndex; leftIndex++) {
                PrimitiveType left = numericTypeOrder[leftIndex];
                for (int rightIndex = 0; rightIndex <= leftIndex; rightIndex++) {
                    PrimitiveType right = numericTypeOrder[rightIndex];
                    addRule(TokenType.ADD, left, right, type);
                    addRule(TokenType.SUB, left, right, type);
                    addRule(TokenType.MUL, left, right, type);
                    addRule(TokenType.DIV, left, right, type);
                    addRule(TokenType.EQUAL, left, right, PrimitiveType.BOOL);
                    addRule(TokenType.NOT_EQUAL, left, right, PrimitiveType.BOOL);
                    addRule(TokenType.LT, left, right, PrimitiveType.BOOL);
                    addRule(TokenType.LESS_EQUAL, left, right, PrimitiveType.BOOL);
                    addRule(TokenType.GT, left, right, PrimitiveType.BOOL);
                    addRule(TokenType.GREATER_EQUAL, left, right, PrimitiveType.BOOL);
                }
            }
        }

        // Special rules for STRING concatenation
        addRule(TokenType.ADD, PrimitiveType.STRING, PrimitiveType.STRING, PrimitiveType.STRING);

        // Allow concatenation with numeric types (e.g., "abc" + 123 = "abc123")
        for (PrimitiveType numericType : numericTypeOrder) {
            addRule(TokenType.ADD, PrimitiveType.STRING, numericType, PrimitiveType.STRING);
            addRule(TokenType.ADD, numericType, PrimitiveType.STRING, PrimitiveType.STRING);
        }

        // String equality rules
        addRule(TokenType.EQUAL, PrimitiveType.STRING, PrimitiveType.STRING, PrimitiveType.BOOL);
        addRule(TokenType.NOT_EQUAL, PrimitiveType.STRING, PrimitiveType.STRING, PrimitiveType.BOOL);
        for (PrimitiveType numericType : numericTypeOrder) {
            addRule(TokenType.EQUAL, PrimitiveType.STRING, numericType, PrimitiveType.BOOL);
            addRule(TokenType.NOT_EQUAL, PrimitiveType.STRING, numericType, PrimitiveType.BOOL);
            addRule(TokenType.EQUAL, numericType, PrimitiveType.STRING, PrimitiveType.BOOL);
            addRule(TokenType.NOT_EQUAL, numericType, PrimitiveType.STRING, PrimitiveType.BOOL);
        }

        // Modulo rules for integer types, promoting BYTE and SHORT to INT
        PrimitiveType[] integerTypes = {
                PrimitiveType.LONG,
                PrimitiveType.INT,
                PrimitiveType.SHORT,
                PrimitiveType.BYTE
        };

        for (int resultIndex = 0; resultIndex < integerTypes.length; resultIndex++) {
            PrimitiveType type = integerTypes[resultIndex];
            for (int leftIndex = 0; leftIndex <= resultIndex; leftIndex++) {
                PrimitiveType left = integerTypes[leftIndex];
                for (int rightIndex = 0; rightIndex <= leftIndex; rightIndex++) {
                    PrimitiveType right = integerTypes[rightIndex];
                    // Result type is the largest of the three, but at least INT
                    PrimitiveType result = (type == PrimitiveType.BYTE || type == PrimitiveType.SHORT) ? PrimitiveType.INT : type;
                    addRule(TokenType.MOD, left, right, result);
                }
            }
        }
    }

    private static void addRule(TokenType operator, PrimitiveType allSame) {
        addRule(operator, allSame, allSame);
    }

    private static void addRule(TokenType operator, PrimitiveType input, PrimitiveType result) {
        addRule(operator, input, input, result, true);
    }

    private static void addRule(TokenType operator, PrimitiveType left, PrimitiveType right, PrimitiveType result) {
        addRule(operator, left, right, result, true);
    }

    private static void addRule(TokenType operator, PrimitiveType left, PrimitiveType right, PrimitiveType result, boolean symmetric) {
        BINARY_OPERATOR_RULES.computeIfAbsent(operator, k -> new BiMap<>()).put(left, right, result);
        if (symmetric)
            BINARY_OPERATOR_RULES.computeIfAbsent(operator, k -> new BiMap<>()).put(right, left, result);
    }

    public static PrimitiveType checkBinaryExpression(Token operator, PrimitiveType left, PrimitiveType right) {
        BiMap<PrimitiveType, PrimitiveType, PrimitiveType> rules = BINARY_OPERATOR_RULES.get(operator.type());
        if (rules == null)
            throw new SemanticException(operator, "Invalid binary operator: " + operator.type().name());

        PrimitiveType result = rules.get(left, right);
        if (result == null)
            throw new SemanticException(operator, "Invalid binary operator: " + operator.type().name() + " for types: " + left.name() + " and " + right.name());

        return result;
    }

    public static PrimitiveType checkUnaryExpression(Token operator, PrimitiveType left) {
        if (operator.type() == TokenType.SUB || operator.type() == TokenType.ADD) {
            if (left == PrimitiveType.STRING)
                throw new SemanticException(operator, "Invalid unary operator: " + operator.type().name() + " for type: " + left.name());

            return left;
        } else if (operator.type() == TokenType.NOT) {
            if (left != PrimitiveType.BOOL)
                throw new SemanticException(operator, "Invalid unary operator: " + operator.type().name() + " for type: " + left.name());

            return PrimitiveType.BOOL;
        }

        throw new SemanticException(operator, "Invalid unary operator: " + operator.type().name());
    }
}
