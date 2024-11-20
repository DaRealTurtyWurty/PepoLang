package dev.turtywurty.pepolang.lexer;

import java.io.IOException;

public interface StreamReader {
    char peek() throws IOException;
    char peek(int k) throws IOException;

    char consume() throws IOException;
    char consume(int k) throws IOException;
}
