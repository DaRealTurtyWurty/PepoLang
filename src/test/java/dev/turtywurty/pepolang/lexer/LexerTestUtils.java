package dev.turtywurty.pepolang.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexerTestUtils {
    public static void assertToken(Token token, TokenType expectedType, String expectedValue) {
        assertEquals(expectedType, token.type());
        assertEquals(expectedValue, token.value());
    }

    public static void assertEmptyValueToken(Token token, TokenType expectedType) {
        assertToken(token, expectedType, "");
    }
}
