package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;

public class NumberTest {
    @Test
    public void testNumbers() {
        Lexer lexer = new Lexer("123 45.67 89.0F 10.11D");
        assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "123");
        assertToken(lexer.nextToken(), TokenType.NUMBER_DOUBLE, "45.67");
        assertToken(lexer.nextToken(), TokenType.NUMBER_FLOAT, "89.0F");
        assertToken(lexer.nextToken(), TokenType.NUMBER_DOUBLE, "10.11D");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testIntegers() {
        Lexer lexer = new Lexer("123 456 789 38393904");
        assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "123");
        assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "456");
        assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "789");
        assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "38393904");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testDoubles() {
        Lexer lexer = new Lexer("123.456 456.789D 789.38393904 38393904.123d");
        assertToken(lexer.nextToken(), TokenType.NUMBER_DOUBLE, "123.456");
        assertToken(lexer.nextToken(), TokenType.NUMBER_DOUBLE, "456.789D");
        assertToken(lexer.nextToken(), TokenType.NUMBER_DOUBLE, "789.38393904");
        assertToken(lexer.nextToken(), TokenType.NUMBER_DOUBLE, "38393904.123d");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testFloats() {
        Lexer lexer = new Lexer("123.456F 456.789f 789.38393904f 38393904.123f");
        assertToken(lexer.nextToken(), TokenType.NUMBER_FLOAT, "123.456F");
        assertToken(lexer.nextToken(), TokenType.NUMBER_FLOAT, "456.789f");
        assertToken(lexer.nextToken(), TokenType.NUMBER_FLOAT, "789.38393904f");
        assertToken(lexer.nextToken(), TokenType.NUMBER_FLOAT, "38393904.123f");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }
}
