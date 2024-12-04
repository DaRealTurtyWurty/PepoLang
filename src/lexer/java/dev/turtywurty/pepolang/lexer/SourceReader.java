package dev.turtywurty.pepolang.lexer;

import java.io.IOException;
import java.io.InputStream;

// TODO: Integrate ring buffer for better performance
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
    public byte peekByte(int k) {
        if (this.pos + k >= this.src.length)
            return '\0';

        return this.src[this.pos + k];
    }

    @Override
    public byte consumeByte(int k) {
        if (this.pos + k >= this.src.length)
            return '\0';

        this.pos += k;
        return this.src[this.pos];
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
