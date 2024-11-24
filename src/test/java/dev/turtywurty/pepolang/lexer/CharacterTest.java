package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;

public class CharacterTest {
    @Test
    void testReadSingleByteCharacter() {
        // Input: 'a' (ASCII single-byte character)
        byte[] src = {'\'', 'a', '\''};
        Lexer lexer = new Lexer(src);

        assertToken(lexer.nextToken(), TokenType.CHARACTER, "a");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    void testReadMultiByteCharacter_2Bytes() {
        // Input: 'ñ' (U+00F1, 2-byte UTF-8)
        byte[] src = {'\'', (byte) 0xC3, (byte) 0xB1, '\''};
        Lexer lexer = new Lexer(src);

        assertToken(lexer.nextToken(), TokenType.CHARACTER, "ñ");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    void testReadMultiByteCharacter_3Bytes() {
        // Input: 'अ' (U+0905, 3-byte UTF-8)
        byte[] src = {'\'', (byte) 0xE0, (byte) 0xA4, (byte) 0x85, '\''};
        Lexer lexer = new Lexer(src);

        assertToken(lexer.nextToken(), TokenType.CHARACTER, "अ");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    void testReadMultiByteCharacter_4Bytes() {
        // Input: '😀' (U+1F600, 4-byte UTF-8)
        byte[] src = {'\'', (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80, '\''};
        Lexer lexer = new Lexer(src);

        assertToken(lexer.nextToken(), TokenType.CHARACTER, "😀");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    void testReadEscapeSequence() {
        // Input: '\n' (escaped newline)
        byte[] src = {'\'', '\\', 'n', '\''};
        Lexer lexer = new Lexer(src);

        assertToken(lexer.nextToken(), TokenType.CHARACTER, "\n");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    void testReadUnicodeEscapeSequence() {
        // Input: '\u00F1' (escaped 'ñ')
        byte[] src = {'\'', '\\', 'u', '0', '0', 'F', '1', '\''};
        Lexer lexer = new Lexer(src);

        assertToken(lexer.nextToken(), TokenType.CHARACTER, "ñ");
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    void testInvalidEscapeSequence() {
        // Input: '\z' (invalid escape)
        Lexer lexer = new Lexer("'\\z'");

        assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    void testTruncatedInput() {
        // Input: Partial UTF-8 (incomplete 2-byte character)
        byte[] src = {'\'', (byte) 0xC3};
        Lexer lexer = new Lexer(src);

        assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    void testInvalidUtf8ContinuationByte() {
        // Input: Invalid UTF-8 sequence (standalone continuation byte)
        byte[] src = {'\'', (byte) 0x80, '\''};
        Lexer lexer = new Lexer(src);

        assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }

    @Test
    void testInvalidUtf8OverlongEncoding() {
        // Input: Overlong encoding for ASCII 'A' (should be invalid)
        byte[] src = {'\'', (byte) 0xC0, (byte) 0x81, '\''};
        Lexer lexer = new Lexer(src);

        assertEmptyValueToken(lexer.nextToken(), TokenType.ILLEGAL);
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }
}
