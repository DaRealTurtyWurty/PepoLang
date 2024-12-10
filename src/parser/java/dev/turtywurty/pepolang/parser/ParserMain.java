package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.lexer.Lexer;
import dev.turtywurty.pepolang.lexer.Token;

import java.util.List;

public class ParserMain {
    public static void main(String[] args) {
        var lexer = new Lexer("1 + 2 * 3;");
        List<Token> tokens = lexer.lex();

        var parser = new Parser(tokens);
        Expression expression = parser.parseExpr();

        if(parser.hadError())
            return;

        System.out.println(AstPrinter.print(expression));
    }
}
