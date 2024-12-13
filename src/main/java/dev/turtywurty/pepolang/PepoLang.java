package dev.turtywurty.pepolang;

import dev.turtywurty.pepolang.interpreter.Interpreter;
import dev.turtywurty.pepolang.lexer.Lexer;
import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.AstPrinter;
import dev.turtywurty.pepolang.parser.Parser;
import dev.turtywurty.pepolang.parser.Statement;

import java.util.List;

public class PepoLang {
    public static void main(String[] args) {
        var lexer = new Lexer("int a = 5;" +
                "int b = 7;" +
                "print(a + b);");
        List<Token> tokens = lexer.lex();

        var parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        if (parser.hadError())
            return;

        System.out.println(AstPrinter.print(statements));

        var interpreter = new Interpreter();
        interpreter.interpret(statements);
    }
}
