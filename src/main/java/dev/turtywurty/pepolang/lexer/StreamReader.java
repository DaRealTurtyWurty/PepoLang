package dev.turtywurty.pepolang.lexer;

public interface StreamReader {
    byte peekByte(int k);
    byte consumeByte(int k);
    
    default byte peekByte() {
        return peekByte(1);
    }
    
    default char peek() {
        return (char) peekByte();
    }
    
    default char peek(int k) {
        return (char) peekByte(k);
    }

    default byte consumeByte() {
        return consumeByte(1);
    }
    
    default char consume(int k) {
        return (char) consumeByte(k);
    }
    
    default char consume() {
        return (char) consumeByte();
    }
}
