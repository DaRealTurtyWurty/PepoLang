package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;

public class IllegalTest {
    @Test
    public void testIllegal() {
        var lexer = new Lexer("foo $ bar");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }
}
