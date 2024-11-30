package dev.turtywurty.pepolang.lexer;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static dev.turtywurty.pepolang.lexer.LexerTestUtils.assertEmptyValueToken;

public class MultiCharTest {
    @Test
    public void testMultiChar() {
        Map<String, TokenType> map = new HashMap<>();
        TokenType.MULTI_CHAR_TOKENS.forEach((character, entries) -> {
            for (Map.Entry<String, TokenType> entry : entries) {
                String key = entry.getKey();
                TokenType value = entry.getValue();
                map.put(key, value);

                System.out.println(key + " -> " + value);
            }
        });

        var builder = new StringBuilder();
        for (String string : map.keySet()) {
            builder.append(string).append(" ");
        }

        String result = builder.toString().trim();
        System.out.println("\n" + result);
        var lexer = new Lexer(result);
        for (TokenType tokenType : map.values()) {
            assertEmptyValueToken(lexer.nextToken(), tokenType);
        }
    }
}
