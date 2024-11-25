package dev.turtywurty.pepolang.lexer;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class SourceReader implements StreamReader {
    private final byte[] src;
    private int pos = -1;

    public SourceReader(byte[] content) {
        this.src = content;
    }

    public SourceReader(String str) {
        this(str.getBytes());
    }

    public SourceReader(InputStream stream) throws IOException {
        this.src = stream.readAllBytes();
    }

    @Override
    public char peek(int k) {
        if (this.pos + k >= this.src.length) {
            return '\0';
        }

        return (char) this.src[this.pos + k];
    }

    @Override
    public char peek() {
        return peek(1);
    }

    @Override
    public char consume(int k) {
        if (this.pos + k >= this.src.length) {
            return '\0';
        }

        this.pos += k;
        return (char) this.src[this.pos];
    }

    @Override
    public char consume() {
        return consume(1);
    }

    public int getPos() {
        return this.pos;
    }

    public boolean hasNext() {
        return this.pos < this.src.length - 1;
    }

    public boolean hasNext(int k) {
        return this.pos + k < this.src.length;
    }
}
