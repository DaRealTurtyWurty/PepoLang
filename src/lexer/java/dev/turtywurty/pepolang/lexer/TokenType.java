package dev.turtywurty.pepolang.lexer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TokenType {
    // Special tokens
    EOF,
    ILLEGAL,
    IDENTIFIER,

    // Numbers
    NUMBER_INT,
    NUMBER_FLOAT,
    NUMBER_DOUBLE,
    NUMBER_LONG,
    NUMBER_BINARY,
    NUMBER_OCTAL,
    NUMBER_HEXADECIMAL,

    // Strings
    STRING,
    MULTI_LINE_STRING,
    CHARACTER,

    // Delimiters
    SEMICOLON,
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    LBRACKET,
    RBRACKET,
    COMMA,
    DOT,

    // Arithmetic
    ADD,
    SUB,
    MUL,
    DIV,
    MOD,

    // Comparison
    ASSIGN,
    NOT,
    GT,
    LT,

    // Keywords
    KEYWORD_VOID,
    KEYWORD_INT,
    KEYWORD_FLOAT,
    KEYWORD_BOOL,
    KEYWORD_STRING,
    KEYWORD_IF,
    KEYWORD_ELSE,
    KEYWORD_WHILE,
    KEYWORD_FOR,
    KEYWORD_RETURN,
    KEYWORD_BREAK,
    KEYWORD_CONTINUE,
    KEYWORD_TRUE,
    KEYWORD_FALSE,
    KEYWORD_NULL,
    KEYWORD_IMPORT,
    KEYWORD_CLASS,
    KEYWORD_DOUBLE,
    KEYWORD_LONG,
    KEYWORD_BYTE,
    KEYWORD_SHORT,
    KEYWORD_CHAR,
    KEYWORD_FINAL,
    KEYWORD_STATIC,
    KEYWORD_THIS,
    KEYWORD_SUPER,
    KEYWORD_NEW,
    KEYWORD_EXTENDS,

    // Operators
    PLUS_PLUS,
    MINUS_MINUS,
    PLUS_EQUAL,
    MINUS_EQUAL,
    MUL_EQUAL,
    DIV_EQUAL,
    EQUAL,
    NOT_EQUAL,
    GREATER_EQUAL,
    LESS_EQUAL,
    AND,
    OR,
    XOR,
    LEFT_SHIFT,
    RIGHT_SHIFT;

    public static final Map<Character, TokenType> SINGLE_CHAR_TOKENS = new HashMap<>() {{
        put(';', SEMICOLON);
        put('(', LPAREN);
        put(')', RPAREN);
        put('{', LBRACE);
        put('}', RBRACE);
        put('[', LBRACKET);
        put(']', RBRACKET);
        put(',', COMMA);
        put('+', ADD);
        put('-', SUB);
        put('*', MUL);
        put('/', DIV);
        put('%', MOD);
        put('=', ASSIGN);
        put('!', NOT);
        put('>', GT);
        put('<', LT);
        put('.', DOT);
    }};

    public static final Map<Character, List<Map.Entry<String, TokenType>>> MULTI_CHAR_TOKENS = new HashMap<>() {{
        put('+', List.of(Map.entry("++", PLUS_PLUS), Map.entry("+=", PLUS_EQUAL)));
        put('-', List.of(Map.entry("--", MINUS_MINUS), Map.entry("-=", MINUS_EQUAL)));
        put('*', List.of(Map.entry("*=", MUL_EQUAL)));
        put('/', List.of(Map.entry("/=", DIV_EQUAL)));
        put('=', List.of(Map.entry("==", EQUAL)));
        put('!', List.of(Map.entry("!=", NOT_EQUAL)));
        put('>', List.of(Map.entry(">=", GREATER_EQUAL), Map.entry(">>", RIGHT_SHIFT)));
        put('<', List.of(Map.entry("<=", LESS_EQUAL), Map.entry("<<", LEFT_SHIFT)));
        put('&', List.of(Map.entry("&&", AND)));
        put('|', List.of(Map.entry("||", OR)));
        put('^', List.of(Map.entry("^", XOR)));
    }};

    public static final Map<String, TokenType> KEYWORDS = new HashMap<>() {{
        put("void", KEYWORD_VOID);
        put("int", KEYWORD_INT);
        put("float", KEYWORD_FLOAT);
        put("bool", KEYWORD_BOOL);
        put("string", KEYWORD_STRING);
        put("if", KEYWORD_IF);
        put("else", KEYWORD_ELSE);
        put("while", KEYWORD_WHILE);
        put("for", KEYWORD_FOR);
        put("return", KEYWORD_RETURN);
        put("break", KEYWORD_BREAK);
        put("continue", KEYWORD_CONTINUE);
        put("true", KEYWORD_TRUE);
        put("false", KEYWORD_FALSE);
        put("null", KEYWORD_NULL);
        put("import", KEYWORD_IMPORT);
        put("class", KEYWORD_CLASS);
        put("static", KEYWORD_STATIC);
        put("this", KEYWORD_THIS);
        put("super", KEYWORD_SUPER);
        put("new", KEYWORD_NEW);
        put("extends", KEYWORD_EXTENDS);
    }};

    public boolean isNonDecimalIntegralLiteral() {
        return this == NUMBER_OCTAL || this == NUMBER_HEXADECIMAL || this == NUMBER_BINARY;
    }

    public boolean isStatementKeyword() {
        if (this == KEYWORD_TRUE || this == KEYWORD_FALSE || this == KEYWORD_NULL)
            return false;

        return KEYWORDS.containsValue(this);
    }

    public boolean isTypeKeyword() {
        return this == KEYWORD_INT || this == KEYWORD_FLOAT || this == KEYWORD_BOOL || this == KEYWORD_STRING ||
                this == KEYWORD_DOUBLE || this == KEYWORD_LONG || this == KEYWORD_BYTE || this == KEYWORD_SHORT ||
                this == KEYWORD_CHAR || this == KEYWORD_VOID;
    }
}
