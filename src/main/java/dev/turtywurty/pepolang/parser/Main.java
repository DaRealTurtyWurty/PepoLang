package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.lexer.Lexer;
import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;
import dev.turtywurty.pepolang.tooling.AstPrinter;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        var lexer = new Lexer("1 + 2 * 3");
        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = lexer.nextToken()).type() != TokenType.EOF) {
            tokens.add(token);
        }

        var parser = new Parser(tokens);
        Expression expression = parser.parse();

        if(parser.hadError()) return;

        System.out.println(AstPrinter.print(expression));
    }
}
