package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.lexer.LexerMain;
import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private boolean hadError = false;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    public Expression parseExpr() {
        return expression();
    }

    private Statement declaration() {
        try {
            if (match(TokenType::isVariableTypeKeyword)) {
                return variableDeclaration();
            }

            if(match(TokenType.IDENTIFIER)) {
                // could be a type. for example: Statement s = null;
                return variableDeclaration();
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement variableDeclaration() {
        Token type = previous();
        Token name = consume(TokenType.IDENTIFIER, "Variable name expected.");

        Expression initializer = null;
        if (match(TokenType.ASSIGN)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "';' expected after variable declaration.");
        return new Statement.VariableStatement(initializer, name, type);
    }

    private Statement statement() {
        if (match(TokenType.KEYWORD_PRINT)) return printStatement();
        return expressionStatement();
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Statement.PrintStatement(value);
    }

    private Statement expressionStatement() {
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Statement.ExpressionStatement(value);
    }

    public Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expression = equality();

        if(match(TokenType.ASSIGN)) {
            Token equals = previous();
            Expression value = assignment();

            if(expression instanceof Expression.Variable variable) {
                Token name = variable.getName();
                return new Expression.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expression;
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

        if(match(TokenType.IDENTIFIER)) {
            return new Expression.Variable(previous());
        }

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

    private boolean match(Predicate<TokenType> predicate) {
        if (check(predicate)) {
            advance();
            return true;
        }

        return false;
    }

    private boolean check(TokenType type) {
        return check(token -> token == type);
    }

    private boolean check(Predicate<TokenType> predicate) {
        if (isAtEnd()) return false;
        return predicate.test(peek().type());
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
        return previous(1);
    }

    private Token previous(int offset) {
        return tokens.get(current - offset);
    }

    public boolean hadError() {
        return this.hadError;
    }
}
