package dev.turtywurty.pepolang.lexer;

public record Token(TokenType type, Object value, int pos) {
    public Token(TokenType type, int pos) {
        this(type, null, pos);
    }

    @Override
    public String toString() {
        return String.format("Token(%s, %s, %d)", this.type, this.value, this.pos);
    }
}
