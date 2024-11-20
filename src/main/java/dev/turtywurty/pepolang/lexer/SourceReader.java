package dev.turtywurty.pepolang.lexer;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class SourceReader implements StreamReader, Closeable {
    private final ByteArrayInputStream src;
    private int pos;

    public SourceReader(byte[] content) {
        this.src = new ByteArrayInputStream(content);
    }

    public SourceReader(String str) {
        this(str.getBytes());
    }

    public SourceReader(InputStream stream) throws IOException {
        this.src = new ByteArrayInputStream(stream.readAllBytes());
    }

    @Override
    public void close() throws IOException {
        this.src.close();
    }

    @Override
    public char peek(int k) {
        if (this.pos + k >= this.src.available()) {
            return '\0';
        }

        this.src.mark(this.pos);
        this.src.skip(k);
        char toReturn = (char) this.src.read();
        this.src.reset();
        return toReturn;
    }

    @Override
    public char peek() {
        return peek(0);
    }

    @Override
    public char consume(int k) {
        if (this.pos + k >= this.src.available()) {
            return '\0';
        }

        this.src.skip(k);
        this.pos += k;
        return (char) this.src.read();
    }

    @Override
    public char consume() {
        return consume(1);
    }

    public int getPos() {
        return this.pos;
    }

    public boolean hasNext() {
        return this.pos < this.src.available();
    }
}
