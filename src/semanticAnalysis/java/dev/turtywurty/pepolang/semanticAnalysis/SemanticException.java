package dev.turtywurty.pepolang.semanticAnalysis;

import dev.turtywurty.pepolang.lexer.Token;

public class SemanticException extends RuntimeException {
    private final Token token;
    private final String message;

    public SemanticException(Token token, String message) {
        super(message);
        this.token = token;
        this.message = message;
    }

    public Token getToken() {
        return this.token;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
