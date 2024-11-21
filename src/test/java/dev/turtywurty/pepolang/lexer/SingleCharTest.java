package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;

public class SingleCharTest {
    @Test
    public void testSingleCharacterTokens() {
        Lexer lexer = new Lexer("+-*/=;<>");
        assertEmptyValueToken(lexer.nextToken(), TokenType.ADD);
        assertEmptyValueToken(lexer.nextToken(), TokenType.SUB);
        assertEmptyValueToken(lexer.nextToken(), TokenType.MUL);
        assertEmptyValueToken(lexer.nextToken(), TokenType.DIV);
        assertEmptyValueToken(lexer.nextToken(), TokenType.ASSIGN);
        assertEmptyValueToken(lexer.nextToken(), TokenType.SEMICOLON);
        assertEmptyValueToken(lexer.nextToken(), TokenType.LT);
        assertEmptyValueToken(lexer.nextToken(), TokenType.GT);
        assertEmptyValueToken(lexer.nextToken(), TokenType.EOF);
    }
}
