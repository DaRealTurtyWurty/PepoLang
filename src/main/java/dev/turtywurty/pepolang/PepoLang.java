package dev.turtywurty.pepolang;

import dev.turtywurty.pepolang.interpreter.Interpreter;
import dev.turtywurty.pepolang.interpreter.Resolver;
import dev.turtywurty.pepolang.lexer.Lexer;
import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.Parser;
import dev.turtywurty.pepolang.parser.Statement;
import dev.turtywurty.pepolang.semanticAnalysis.SemanticAnalyzer;
import dev.turtywurty.pepolang.semanticAnalysis.SemanticException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PepoLang {
    public static void main(String[] args) throws IOException {
        String fullSrc = Files.readString(Path.of("E:\\PepoLang\\src\\main\\resources\\main.pepolang"));
        var lexer = new Lexer(fullSrc);
        List<Token> tokens = lexer.lex();

        var parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        if (parser.hadError())
            return;

        //System.out.println(AstPrinter.print(statements));

        var semanticAnalyzer = new SemanticAnalyzer(statements);
        semanticAnalyzer.analyze();

        if (semanticAnalyzer.hadError()) {
            for (SemanticException error : semanticAnalyzer.getErrors()) {
                System.err.println("Error: " + error.getMessage() + " at pos: " + error.getToken().pos());
            }

            return;
        }

        var interpreter = new Interpreter();

        var resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        interpreter.interpret(statements);

//        LLVMCodeGenerator.generate(statements, Path.of("output.ll"));
    }
}
