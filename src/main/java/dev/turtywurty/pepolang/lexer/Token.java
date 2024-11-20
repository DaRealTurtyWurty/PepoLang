package dev.turtywurty.pepolang.lexer;

public record Token(TokenType type, String value, int pos) {
    @Override
    public String toString() {
        return String.format("Token(%s, %s, %d)", this.type, this.value, this.pos);
    }
}
