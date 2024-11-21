package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;

public class CommentTest {
    @Test
    public void testSingleLineComments() {
        Lexer lexer = new Lexer("foo // this is a comment\nbar");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testMultilineComments() {
        Lexer lexer = new Lexer("foo /* this is a\nmultiline comment */ bar");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("foo /* this is a\nmultiline comment\nthat spans multiple lines */ bar");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);

        lexer = new Lexer("foo /* this is an\nillegal\nmultiline comment * bar");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, ""); // but really should be "this is an\nillegal\nmultiline comment * bar"
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }
}
