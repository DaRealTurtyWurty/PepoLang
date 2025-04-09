package dev.turtywurty.pepolang;

import dev.turtywurty.pepolang.codeGeneration.LLVMCodeGenerator;
import dev.turtywurty.pepolang.interpreter.Interpreter;
import dev.turtywurty.pepolang.interpreter.Resolver;
import dev.turtywurty.pepolang.lexer.Lexer;
import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.AstPrinter;
import dev.turtywurty.pepolang.parser.Parser;
import dev.turtywurty.pepolang.parser.Statement;
import dev.turtywurty.pepolang.semanticAnalysis.SemanticAnalyzer;
import dev.turtywurty.pepolang.semanticAnalysis.SemanticException;

import java.nio.file.Path;
import java.util.List;

public class PepoLang {
    public static void main(String[] args) {
        String fullSrc = """
                string determineOutput(int num) {
                    if(num % 3 == 0 && num % 5 == 0) {
                        return "FizzBuzz";
                    } else if(num % 3 == 0) {
                        return "Fizz";
                    } else if(num % 5 == 0) {
                        return "Buzz";
                    }
                
                    return num;
                }
                
                void printTime() {
                    print(time() + "");
                }
                
                void main() {
                    int a = 50;
                    while(a > 0) {
                        if(a < 30 && a > 10) {
                            a = a - 1;
                            continue;
                        }
                
                        for(int i = 0; i < 10; i = i + 1) {
                            string output = determineOutput(i);
                            if(output != i) {
                                print("i: " + i + ", output: " + output);
                            }
                        }
                
                        a = a - 1;
                    }
                
                    printTime();
                }
                
                main();
                """;
        var lexer = new Lexer(fullSrc);
        List<Token> tokens = lexer.lex();

        var parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        if (parser.hadError())
            return;

        System.out.println(AstPrinter.print(statements));

        var semanticAnalyzer = new SemanticAnalyzer(statements);
        semanticAnalyzer.analyze();

        if (semanticAnalyzer.hadError()) {
            for (SemanticException error : semanticAnalyzer.getErrors()) {
                System.err.println("Error: " + error.getMessage() + " at C" + error.getToken().pos());
            }

            return;
        }

//        var interpreter = new Interpreter();
//
//        var resolver = new Resolver(interpreter);
//        resolver.resolve(statements);
//
//        interpreter.interpret(statements);

        LLVMCodeGenerator.generate(statements, Path.of("output.ll"));
    }
}
