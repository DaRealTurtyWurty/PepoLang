package dev.turtywurty.pepolang.semanticAnalysis;

import dev.turtywurty.pepolang.lexer.TokenType;

public enum PrimitiveType {
    INT,
    FLOAT,
    DOUBLE,
    STRING,
    BOOL,
    VOID,
    LONG,
    CHAR,
    BYTE,
    SHORT;

    public static PrimitiveType fromTokenType(TokenType type) {
        return switch (type) {
            case KEYWORD_INT -> INT;
            case KEYWORD_FLOAT -> FLOAT;
            case KEYWORD_DOUBLE -> DOUBLE;
            case KEYWORD_STRING -> STRING;
            case KEYWORD_BOOL -> BOOL;
            case KEYWORD_VOID -> VOID;
            case KEYWORD_LONG -> LONG;
            case KEYWORD_CHAR -> CHAR;
            case KEYWORD_BYTE -> BYTE;
            case KEYWORD_SHORT -> SHORT;
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}