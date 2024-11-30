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

    // Arithmetic
    ADD,
    SUB,
    MUL,
    DIV,

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
        put('=', ASSIGN);
        put('!', NOT);
        put('>', GT);
        put('<', LT);
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
    }};

    public boolean isNonDecimalIntegralLiteral() {
        return this == NUMBER_OCTAL || this == NUMBER_HEXADECIMAL || this == NUMBER_BINARY;
    }
}
