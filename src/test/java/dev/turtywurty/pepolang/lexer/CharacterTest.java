package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;

public class CharacterTest {
    @Test
    public void testCharacter() {
        var lexer = new Lexer("'a'");
        assertToken(lexer.nextToken(), TokenType.CHARACTER, "a");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("'\\n'");
        assertToken(lexer.nextToken(), TokenType.CHARACTER, "\n");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("'\\u0000'");
        assertToken(lexer.nextToken(), TokenType.CHARACTER, "\\u0000");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("'💀'");
        assertToken(lexer.nextToken(), TokenType.CHARACTER, "💀");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }
}
