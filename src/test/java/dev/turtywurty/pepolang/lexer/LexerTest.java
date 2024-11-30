package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LexerTest {
    @Test
    public void testComplex() {
        String content = """
            foo = 123;
            bar = 456;
            baz = foo + bar; // This is a comment
            bar /= foo * 2; // This is another comment
            """;

        var lexer = new Lexer(content);

        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
        assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "123");
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);

        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
        assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "456");
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);

        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "baz");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ADD);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);

        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.DIV_EQUAL);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertEmptyValueToken(lexer.nextToken(), TokenType.MUL);
        assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "2");
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);

        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testEmpty() {
        var lexer = new Lexer("");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testWhitespace() {
        var lexer = new Lexer(" \t\n\r");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testFile() {
        try {
            String content = Files.readString(Path.of("src/test/resources/test.pepolang").toAbsolutePath());
            var lexer = new Lexer(content);

            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_CLASS);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "Test");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_INT);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "a");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_STRING);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "b");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_VOID);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "main");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LBRACE);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "a");
            assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
            assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "1");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "b");
            assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
            assertToken(lexer.nextToken(), TokenType.STRING, "Hello, World!");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "print");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "a");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "print");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "b");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_INT);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "c");
            assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "add");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "1");
            assertEmptyValueToken(lexer.nextToken(), TokenType.COMMA);
            assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "2");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "print");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "c");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_INT);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "d");
            assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "add");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "3");
            assertEmptyValueToken(lexer.nextToken(), TokenType.COMMA);
            assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "4");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "print");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "d");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_INT);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "c");
            assertEmptyValueToken(lexer.nextToken(), TokenType.MUL);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "d");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertEmptyValueToken(lexer.nextToken(), TokenType.MUL_EQUAL);
            assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "5");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_IF);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertEmptyValueToken(lexer.nextToken(), TokenType.GT);
            assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "100");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LBRACE);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "print");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.STRING, "e is greater than 100");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_ELSE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LBRACE);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "print");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.STRING, "e is less than or equal to 100");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_WHILE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertEmptyValueToken(lexer.nextToken(), TokenType.GT);
            assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "0");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LBRACE);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertEmptyValueToken(lexer.nextToken(), TokenType.MINUS_EQUAL);
            assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "1");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "print");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_INT);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "add");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_INT);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "a");
            assertEmptyValueToken(lexer.nextToken(), TokenType.COMMA);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_INT);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "b");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_RETURN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "a");
            assertEmptyValueToken(lexer.nextToken(), TokenType.ADD);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "b");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
        } catch (IOException exception) {
            assertDoesNotThrow(() -> {
                throw new RuntimeException(exception);
            });
        }
    }
}
