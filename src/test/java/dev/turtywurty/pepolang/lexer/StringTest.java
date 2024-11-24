package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;

public class StringTest {
    @Test
    public void testString() {
        var lexer = new Lexer("\"Hello, World!\"");
        assertToken(lexer.nextToken(), TokenType.STRING, "Hello, World!");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("\"Hello, World!\\n\"");
        assertToken(lexer.nextToken(), TokenType.STRING, "Hello, World!\n");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("\"Hello, World!\n\"");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "Hello, World!");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testMultiLineString() {
        var lexer = new Lexer("`Hello, World!`");
        assertToken(lexer.nextToken(), TokenType.MULTI_LINE_STRING, "Hello, World!");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("`Hello, World!\n`");
        assertToken(lexer.nextToken(), TokenType.MULTI_LINE_STRING, "Hello, World!\n");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("`Hello,\nW\no\nr\nl\nd\n!`");
        assertToken(lexer.nextToken(), TokenType.MULTI_LINE_STRING, "Hello,\nW\no\nr\nl\nd\n!");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("`Hello, World!");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "Hello, World!");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }
}
