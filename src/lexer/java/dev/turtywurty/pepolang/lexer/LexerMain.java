package dev.turtywurty.pepolang.lexer;

import dev.turtywurty.pepolang.logging.LogManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class LexerMain {
    public static void main(String[] args) {
        if(args.length == 0) {
            System.err.println("Please provide a file to parse!");
            return;
        }

        String file = args[0];
        Path path;
        try {
            path = Path.of(file);
        } catch (InvalidPathException ignored) {
            System.err.printf("Invalid file path: %s%n", file);
            return;
        }

        if(Files.notExists(path)) {
            System.err.printf("File does not exist: %s%n", file);
            return;
        }

        if(!Files.isReadable(path)) {
            System.err.printf("File is not readable: %s%n", file);
            return;
        }

        String content;
        try {
            content = Files.readString(path);
        } catch (IOException exception) {
            String message = exception.getMessage();
            System.err.printf("An error occurred while reading the file: \"%s\"%n", message);
            return;
        }

        var lexer = new Lexer(content);
        Token token;
        while ((token = lexer.nextToken()).type() != TokenType.EOF) {
            System.out.println(token);
        }

        System.out.println(token);
    }

    public static void main2(String[] args) {
        String content = """
            foo = 123;
            bar = 456;
            baz = foo + bar; // This is a comment
            bar /= foo * 2; // This is another comment
            """;

        var lexer = new Lexer(content);
        Token token;
        while ((token = lexer.nextToken()).type() != TokenType.EOF) {
            System.out.println(token);
        }

        System.out.println(token);
    }

    public static void main3(String[] args) {
        var content = """
            void int float bool string 40 if else while for + return break continue true false null import class foo;
            barvoid integer / floaty boolean stringy 69.420 // if else \n, (int jazz)
            iffy elsey whiley fory returny breaky continuey truey falsey nully importy classy
          """;

        var lexer = new Lexer(content);
        Token token;
        while ((token = lexer.nextToken()).type() != TokenType.EOF) {
            System.out.println(token);
        }

        System.out.println(token);
    }

    public static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            LogManager.report(token.pos(), " at end", message);
        } else {
            LogManager.report(token.pos(), " at '" + token.value() + "'", message);
        }
    }
}