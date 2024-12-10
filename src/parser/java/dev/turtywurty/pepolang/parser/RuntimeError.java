package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.lexer.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public RuntimeError(String message) {
        super(message);
        this.token = null;
    }
}