package dev.turtywurty.pepolang;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;

public class PepoLang {
    public static void main(String[] args) {
        
    }

    public static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.pos(), " at end", message);
        } else {
            report(token.pos(), " at '" + token.value() + "'", message);
        }
    }

    public static void report(int pos, String where, String message) {
        System.err.println("[pos " + pos + "] Error" + where + ": " + message);
    }
}
