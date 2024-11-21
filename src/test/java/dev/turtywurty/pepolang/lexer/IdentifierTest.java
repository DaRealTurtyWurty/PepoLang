package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;

public class IdentifierTest {
    @Test
    public void testIdentifiers() {
        Lexer lexer = new Lexer("foo bar _baz");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "_baz");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }
}
