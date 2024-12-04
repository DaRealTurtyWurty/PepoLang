package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.lexer.LexerMain;
import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private boolean hadError = false;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expression parse() {
        return null;
    }

    public Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression expression = comparison();

        while(match(TokenType.NOT_EQUAL, TokenType.EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression comparison() {
        Expression expression = term();

        while (match(TokenType.GT, TokenType.LT, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression term() {
        Expression expression = factor();

        while (match(TokenType.ADD, TokenType.SUB)) {
            Token operator = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor() {
        Expression expression = unary();

        while (match(TokenType.DIV, TokenType.MUL)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary() {
        if(match(TokenType.NOT, TokenType.SUB, TokenType.ADD)) {
            Token operator = previous();
            Expression expression = unary();
            new Expression.Unary(operator, expression);
        }

        return primary();
    }

    private Expression primary() {
        if (match(TokenType.KEYWORD_FALSE)) return new Expression.Literal(false);
        if (match(TokenType.KEYWORD_TRUE)) return new Expression.Literal(true);
        if (match(TokenType.KEYWORD_NULL)) return new Expression.Literal(null);

        if(match(TokenType.NUMBER_INT, TokenType.NUMBER_HEXADECIMAL, TokenType.NUMBER_BINARY, TokenType.NUMBER_OCTAL, TokenType.NUMBER_FLOAT, TokenType.NUMBER_DOUBLE, TokenType.NUMBER_LONG, TokenType.STRING, TokenType.MULTI_LINE_STRING, TokenType.CHARACTER))
            return new Expression.Literal(previous().value());

        if(match(TokenType.LPAREN)) {
            Expression expression = expression();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peek(), "Expression expected.");
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type() == TokenType.SEMICOLON) return;

            if(peek().type().isStatementKeyword())
                return;

            advance();
        }
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        LexerMain.error(token, message);
        return new ParseError();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    public boolean hadError() {
        return this.hadError;
    }
}
