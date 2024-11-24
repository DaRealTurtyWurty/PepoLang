package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;

public class NumberTest {
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

    @Test
    public void testLongs() {
        Lexer lexer = new Lexer("123L 456l 789L 38393904l");
        assertToken(lexer.nextToken(), TokenType.NUMBER_LONG, "123L");
        assertToken(lexer.nextToken(), TokenType.NUMBER_LONG, "456l");
        assertToken(lexer.nextToken(), TokenType.NUMBER_LONG, "789L");
        assertToken(lexer.nextToken(), TokenType.NUMBER_LONG, "38393904l");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testBinary() {
        Lexer lexer = new Lexer("0b101 0B101 0b101 0B101 0b101010101001010");
        assertToken(lexer.nextToken(), TokenType.NUMBER_BINARY, "0b101");
        assertToken(lexer.nextToken(), TokenType.NUMBER_BINARY, "0B101");
        assertToken(lexer.nextToken(), TokenType.NUMBER_BINARY, "0b101");
        assertToken(lexer.nextToken(), TokenType.NUMBER_BINARY, "0B101");
        assertToken(lexer.nextToken(), TokenType.NUMBER_BINARY, "0b101010101001010");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testOctal() {
        Lexer lexer = new Lexer("034221 01613 00126 01542452");
        assertToken(lexer.nextToken(), TokenType.NUMBER_OCTAL, "034221");
        assertToken(lexer.nextToken(), TokenType.NUMBER_OCTAL, "01613");
        assertToken(lexer.nextToken(), TokenType.NUMBER_OCTAL, "00126");
        assertToken(lexer.nextToken(), TokenType.NUMBER_OCTAL, "01542452");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testHex() {
        Lexer lexer = new Lexer("0x26AE 0X995FF 0x163D 0X1A 0X123 0x123ABCDEF");
        assertToken(lexer.nextToken(), TokenType.NUMBER_HEXADECIMAL, "0x26AE");
        assertToken(lexer.nextToken(), TokenType.NUMBER_HEXADECIMAL, "0X995FF");
        assertToken(lexer.nextToken(), TokenType.NUMBER_HEXADECIMAL, "0x163D");
        assertToken(lexer.nextToken(), TokenType.NUMBER_HEXADECIMAL, "0X1A");
        assertToken(lexer.nextToken(), TokenType.NUMBER_HEXADECIMAL, "0X123");
        assertToken(lexer.nextToken(), TokenType.NUMBER_HEXADECIMAL, "0x123ABCDEF");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    public void testInvalidNumbers() {
        Lexer lexer = new Lexer("00x0 1b5 0b2 5L3 0x1.5 0b1.5 5..2 8.24.6");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "00x0");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "1b5");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "0b2");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "5L3");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "0x1.5");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "0b1.5");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "5..2");
        assertToken(lexer.nextToken(), TokenType.ILLEGAL, "8.24.6");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }
}
