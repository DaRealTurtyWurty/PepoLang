package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;
import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertToken;

public class KeywordTest {
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
        assertToken(lexer.nextToken(), TokenType.NUMBER_INT, "40");
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
        assertToken(lexer.nextToken(), TokenType.NUMBER_DOUBLE, "69.420");
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
}
