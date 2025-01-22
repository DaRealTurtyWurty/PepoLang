package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.lexer.LexerMain;
import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private boolean hadError = false;
    private int loopDepth = 0;

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
            if (match(TokenType::isTypeKeyword) && check(TokenType.IDENTIFIER))
                return variableOrFunctionDeclaration();

            if (check(TokenType.IDENTIFIER)) {
                if (check(TokenType.ASSIGN, 1)) {
                    advance(); // we didn't match so we need to advance to the next token
                    return assignStatement();
                }

                if(check(TokenType.IDENTIFIER, 1)) {
                    advance(); // we didn't match so we need to advance to the next token

                    // could be a type. for example: Statement s = null;
                    return variableOrFunctionDeclaration();
                }
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement variableOrFunctionDeclaration() {
        Token type = previous();
        Token name = consume(TokenType.IDENTIFIER, "Expected function or variable name.");
        if(match(TokenType.LPAREN)) {
            return functionDeclaration(type, name);
        }

        if(type.type() == TokenType.KEYWORD_VOID)
            throw error(type, "Cannot declare a variable with type 'void'.");

        return variableDeclaration(type, name);
    }

    private Statement functionDeclaration(Token type, Token name) {
        Map<Token, Token> parameters = new HashMap<>();
        if (!check(TokenType.RPAREN)) {
            do {
                Token parameterType = consume(TokenType::isTypeKeyword, "Expected parameter type.");
                Token parameterName = consume(TokenType.IDENTIFIER, "Expected parameter name.");
                parameters.put(parameterName, parameterType);
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Expected ')' after function parameters.");
        consume(TokenType.LBRACE, "Expected '{' before function body.");

        List<Statement> body = block();
        return new Statement.FunctionStatement(body, name, parameters, type);
    }

    private Statement variableDeclaration(Token type, Token name) {
        Expression initializer = null;
        if (match(TokenType.ASSIGN)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.");
        return new Statement.VariableStatement(initializer, name, type);
    }

    private Statement assignStatement() {
        Token name = previous();
        consume(TokenType.ASSIGN, "Expected '=' after variable name.");
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after value.");

        return new Statement.AssignStatement(name, value);
    }

    private Statement statement() {
        if (match(TokenType.KEYWORD_BREAK)) return breakStatement();
        if (match(TokenType.KEYWORD_CONTINUE)) return continueStatement();
        if (match(TokenType.KEYWORD_FOR)) return forStatement();
        if (match(TokenType.KEYWORD_IF)) return ifStatement();
        if (match(TokenType.KEYWORD_RETURN)) return returnStatement();
        if (match(TokenType.KEYWORD_WHILE)) return whileStatement();
        if (match(TokenType.LBRACE)) return new Statement.BlockStatement(block());
        return expressionStatement();
    }

    private Statement breakStatement() {
        if(this.loopDepth == 0) {
            throw error(previous(), "Cannot use 'break' outside of a loop.");
        }

        consume(TokenType.SEMICOLON, "Expected ';' after 'break'.");
        return new Statement.BreakStatement();
    }

    private Statement continueStatement() {
        if(this.loopDepth == 0) {
            throw error(previous(), "Cannot use 'continue' outside of a loop.");
        }

        consume(TokenType.SEMICOLON, "Expected ';' after 'continue'.");
        return new Statement.ContinueStatement();
    }

    private Statement forStatement() {
        consume(TokenType.LPAREN, "Expected '(' after 'for'.");
        this.loopDepth++;

        try {
            Statement initializer;
            if (match(TokenType.SEMICOLON)) {
                initializer = null;
            } else if (match(TokenType::isTypeKeyword)) {
                initializer = variableOrFunctionDeclaration();
            } else {
                initializer = expressionStatement();
            }

            Expression condition = null;
            if (!check(TokenType.SEMICOLON)) {
                condition = expression();
            }

            consume(TokenType.SEMICOLON, "Expected ';' after loop condition.");

            Expression incrementer = null;
            if (!check(TokenType.RPAREN)) {
                incrementer = expression();
            }

            consume(TokenType.RPAREN, "Expected ')' after for clause.");

            // Increment the loop depth before parsing the body so that 'break' and 'continue' statements can be checked
            this.loopDepth++;
            Statement body = statement();
            this.loopDepth--;

            // Add incrementer to the end of the body
            if (incrementer != null)
                body = new Statement.BlockStatement(List.of(body, new Statement.ExpressionStatement(incrementer)));

            // Add condition to the start of the body as a while loop
            if (condition == null)
                condition = new Expression.Literal(true);
            body = new Statement.WhileStatement(body, condition);

            // Add initializer to the start of the body
            if(initializer != null)
                body = new Statement.BlockStatement(List.of(initializer, body));

            return body;
        } finally {
            this.loopDepth--;
        }
    }

    private Statement ifStatement() {
        consume(TokenType.LPAREN, "Expected '(' after 'if'.");
        Expression condition = expression();
        consume(TokenType.RPAREN, "Expected ')' after condition.");

        Statement thenBranch = statement();
        Statement elseBranch = null;

        if (match(TokenType.KEYWORD_ELSE)) {
            elseBranch = statement();
        }

        return new Statement.IfStatement(condition, elseBranch, thenBranch);
    }

    private Statement returnStatement() {
        Token keyword = previous();
        Expression value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after return value.");
        return new Statement.ReturnStatement(keyword, value);
    }

    private Statement whileStatement() {
        consume(TokenType.LPAREN, "Expected '(' after 'while'.");

        this.loopDepth++;
        try {

            Expression condition = expression();
            consume(TokenType.RPAREN, "Expected ')' after condition.");

            Statement body = statement();
            return new Statement.WhileStatement(body, condition);
        } finally {
            this.loopDepth--;
        }
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RBRACE, "Expected '}' after block.");
        return statements;
    }

    private Statement expressionStatement() {
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after value.");
        return new Statement.ExpressionStatement(value);
    }

    public Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expression = or();

        if (match(TokenType.ASSIGN)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable variable) {
                Token name = variable.getName();
                return new Expression.Assign(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    private Expression or() {
        Expression expression = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and() {
        Expression expression = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression equality() {
        Expression expression = comparison();

        while (match(TokenType.NOT_EQUAL, TokenType.EQUAL)) {
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
        if (match(TokenType.NOT, TokenType.SUB, TokenType.ADD)) {
            Token operator = previous();
            Expression expression = unary();
            new Expression.Unary(operator, expression);
        }

        return call();
    }

    private Expression call() {
        Expression expression = primary();

        while (true) {
            if (match(TokenType.LPAREN)) {
                expression = finishCall(expression);
            } else {
                break;
            }
        }

        return expression;
    }

    private Expression finishCall(Expression callee) {
        List<Expression> arguments = new ArrayList<>();
        if(!check(TokenType.RPAREN)) {
            do {
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RPAREN, "Expected ')' after arguments.");
        return new Expression.Call(arguments, callee, paren);
    }

    private Expression primary() {
        if (match(TokenType.KEYWORD_FALSE)) return new Expression.Literal(false);
        if (match(TokenType.KEYWORD_TRUE)) return new Expression.Literal(true);
        if (match(TokenType.KEYWORD_NULL)) return new Expression.Literal(null);

        if (match(TokenType.NUMBER_INT, TokenType.NUMBER_HEXADECIMAL, TokenType.NUMBER_BINARY, TokenType.NUMBER_OCTAL, TokenType.NUMBER_FLOAT, TokenType.NUMBER_DOUBLE, TokenType.NUMBER_LONG, TokenType.STRING, TokenType.MULTI_LINE_STRING, TokenType.CHARACTER))
            return new Expression.Literal(previous().value());

        if (match(TokenType.IDENTIFIER)) {
            Token name = previous();
            if(match(TokenType.LPAREN)) {
                return finishCall(new Expression.Function(name));
            }

            return new Expression.Variable(name);
        }

        if (match(TokenType.LPAREN)) {
            Expression expression = expression();
            consume(TokenType.RPAREN, "Expected ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peek(), "Expression expected.");
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type() == TokenType.SEMICOLON) return;

            if (peek().type().isStatementKeyword())
                return;

            advance();
        }
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private Token consume(Predicate<TokenType> predicate, String message) {
        if (check(predicate)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        this.hadError = true;
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
        return check(type, 0);
    }

    private boolean check(TokenType type, int offset) {
        return check(token -> token == type, offset);
    }

    private boolean check(TokenType... types) {
        for(int offset = 0; offset < types.length; offset++) {
            if(check(types[offset], offset)) {
                return true;
            }
        }

        return false;
    }

    private boolean check(Predicate<TokenType> predicate, int offset) {
        if (isAtEnd()) return false;
        return predicate.test(peek(offset).type());
    }

    private boolean check(Predicate<TokenType> predicate) {
        return check(predicate, 0);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return peek(0);
    }

    private Token peek(int offset) {
        return tokens.get(current + offset);
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
