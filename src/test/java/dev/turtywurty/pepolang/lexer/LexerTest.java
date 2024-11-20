package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexerTest {
    @Test
    public void testSingleCharacterTokens() {
        Lexer lexer = new Lexer("+-*/=;<>");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ADD);
        assertEmptyValueToken(lexer.nextToken(), TokenType.SUB);
        assertEmptyValueToken(lexer.nextToken(), TokenType.MUL);
        assertEmptyValueToken(lexer.nextToken(), TokenType.DIV);
        assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testIdentifiers() {
        Lexer lexer = new Lexer("foo bar _baz");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "_baz");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testNumbers() {
        Lexer lexer = new Lexer("123 45.67");
        assertToken(lexer.nextToken(), TokenType.NUMBER, "123");
        assertToken(lexer.nextToken(), TokenType.NUMBER, "45.67");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testComments() {
        Lexer lexer = new Lexer("foo // this is a comment\nbar");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testKeywords() {
        var content = """
            void int float bool string 40 if else while for + return break continue true false null import class foo;
            barvoid integer / floaty boolean stringy 69.420 // if else \n, (int jazz) 
            iffy elsey whiley fory returny breaky continuey truey falsey nully importy classy
            """;

        var lexer = new Lexer(content);

        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_VOID);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_INT);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_FLOAT);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_BOOL);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_STRING);
        assertToken(lexer.nextToken(), TokenType.NUMBER, "40");
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_IF);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_ELSE);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_WHILE);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_FOR);
        assertEmptyValueToken(lexer.nextToken(), TokenType.ADD);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_RETURN);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_BREAK);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_CONTINUE);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_TRUE);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_FALSE);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_NULL);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_IMPORT);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_CLASS);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);

        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "barvoid");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "integer");
        assertEmptyValueToken(lexer.nextToken(), TokenType.DIV);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "floaty");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "boolean");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "stringy");
        assertToken(lexer.nextToken(), TokenType.NUMBER, "69.420");
        assertEmptyValueToken(lexer.nextToken(), TokenType.COMMA);
        assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
        assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_INT);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "jazz");
        assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);

        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "iffy");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "elsey");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "whiley");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "fory");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "returny");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "breaky");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "continuey");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "truey");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "falsey");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "nully");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "importy");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "classy");

        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

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
        assertToken(lexer.nextToken(), TokenType.NUMBER, "123");
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);

        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
        assertToken(lexer.nextToken(), TokenType.NUMBER, "456");
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);

        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "baz");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ADD);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);

        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
        assertEmptyValueToken(lexer.nextToken(), TokenType.DIV);
        assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertEmptyValueToken(lexer.nextToken(), TokenType.MUL);
        assertToken(lexer.nextToken(), TokenType.NUMBER, "2");
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
    public void testIllegal() {
        var lexer = new Lexer("foo $ bar");
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "foo");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
        assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "bar");
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
            assertToken(lexer.nextToken(), TokenType.NUMBER, "1");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "b");
            assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "Hello");
            assertEmptyValueToken(lexer.nextToken(), TokenType.COMMA);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "World");
            assertEmptyValueToken(lexer.nextToken(), TokenType.NOT);
            assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
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
            assertToken(lexer.nextToken(), TokenType.NUMBER, "1");
            assertEmptyValueToken(lexer.nextToken(), TokenType.COMMA);
            assertToken(lexer.nextToken(), TokenType.NUMBER, "2");
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
            assertToken(lexer.nextToken(), TokenType.NUMBER, "3");
            assertEmptyValueToken(lexer.nextToken(), TokenType.COMMA);
            assertToken(lexer.nextToken(), TokenType.NUMBER, "4");
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
            assertEmptyValueToken(lexer.nextToken(), TokenType.MUL);
            assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
            assertToken(lexer.nextToken(), TokenType.NUMBER, "5");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_IF);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertEmptyValueToken(lexer.nextToken(), TokenType.GT);
            assertToken(lexer.nextToken(), TokenType.NUMBER, "100");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LBRACE);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "print");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "is");
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "greater");
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "than");
            assertToken(lexer.nextToken(), TokenType.NUMBER, "100");
            assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_ELSE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LBRACE);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "print");
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "is");
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "less");
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "than");
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "or");
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "equal");
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "to");
            assertToken(lexer.nextToken(), TokenType.NUMBER, "100");
            assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
            assertEmptyValueToken(lexer.nextToken(), TokenType.RBRACE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.KEYWORD_WHILE);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LPAREN);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertEmptyValueToken(lexer.nextToken(), TokenType.GT);
            assertToken(lexer.nextToken(), TokenType.NUMBER, "0");
            assertEmptyValueToken(lexer.nextToken(), TokenType.RPAREN);
            assertEmptyValueToken(lexer.nextToken(), TokenType.LBRACE);
            assertToken(lexer.nextToken(), TokenType.IDENTIFIER, "e");
            assertEmptyValueToken(lexer.nextToken(), TokenType.SUB);
            assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
            assertToken(lexer.nextToken(), TokenType.NUMBER, "1");
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
            exception.printStackTrace();
        }
    }

    private void assertToken(Token token, TokenType expectedType, String expectedValue) {
        assertEquals(expectedType, token.type());
        assertEquals(expectedValue, token.value());
    }

    private void assertEmptyValueToken(Token token, TokenType expectedType) {
        assertToken(token, expectedType, "");
    }
}
