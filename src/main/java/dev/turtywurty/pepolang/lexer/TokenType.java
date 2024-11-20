package dev.turtywurty.pepolang.lexer;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    EOF,
    ILLEGAL,
    IDENTIFIER,
    NUMBER,

    SEMICOLON,
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    LBRACKET,
    RBRACKET,
    COMMA,

    ADD,
    SUB,
    MUL,
    DIV,

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
    KEYWORD_CLASS;

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
}
