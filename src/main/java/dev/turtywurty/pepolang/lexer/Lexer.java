package dev.turtywurty.pepolang.lexer;

import java.nio.charset.StandardCharsets;

// TODO: Circular Buffer
// TODO: peek() and peek(k) methods
// TODO: consume() and consume(k) methods
// TODO: Add support for strings (might be quite complex because of escape characters)
// TODO: Support for multi-line comments
// TODO: Support for multi-line strings
// TODO: Split the reader into a separate class that can take a string, byte[] or InputStream
// TODO: Number literals should be parsed into their respective types depending on a suffix (f, d, l, etc.) - or default
// TODO: Add support for binary, octal and hexadecimal literals
// TODO: Support for unicode characters
// TODO: Support for character literals
// TODO: Support for when double or triple characters are used for operators (e.g. ++, --, ==, !=, <=, >=, >>>, <<<, etc.)
public class Lexer {
    private final byte[] src;
    private int pos;

    public Lexer(byte[] content) {
        this.src = content;
    }

    public Lexer(String str) {
        this(str.getBytes(StandardCharsets.UTF_8));
    }

    public Token nextToken() {
        Token toReturn = null;
        while (this.pos < this.src.length) {
            char current = (char) this.src[this.pos];
            if (Character.isWhitespace(current)) {
                this.pos++;
                continue;
            }

            if(current == '/') {
                if((this.pos + 1 < this.src.length) && this.src[this.pos + 1] == '/') { // reached a comment
                    while (this.pos < this.src.length && this.src[this.pos] != '\n') {
                        this.pos++;
                    }

                    continue;
                } else {
                    toReturn = new Token(TokenType.DIV, "", this.pos);
                    break;
                }
            }

            if(TokenType.SINGLE_CHAR_TOKENS.containsKey(current)) {
                toReturn = new Token(TokenType.SINGLE_CHAR_TOKENS.get(current), "", this.pos);
                break;
            }

            if(canStartIdentifier(current)) {
                var identifier = new StringBuilder();
                do {
                    identifier.append(current);

                    if(this.pos++ >= this.src.length)
                        break;

                    current = (char) this.src[this.pos];
                } while (isValidForIdentifier(current));

                this.pos--;

                String identifierStr = identifier.toString();
                if(TokenType.KEYWORDS.containsKey(identifierStr)) {
                    toReturn = new Token(TokenType.KEYWORDS.get(identifierStr), "", this.pos - 1);
                    break;
                }

                toReturn = new Token(TokenType.IDENTIFIER, identifierStr, this.pos - 1);
                break;
            }

            if(Character.isDigit(current)) {
                var number = new StringBuilder();
                do {
                    number.append(current);

                    if(this.pos++ >= this.src.length)
                        break;

                    current = (char) this.src[this.pos];
                } while (Character.isDigit(current) || (current == '.' && number.indexOf(".") == -1 && Character.isDigit((char) this.src[this.pos + 1])));

                this.pos--;
                toReturn = new Token(TokenType.NUMBER, number.toString(), this.pos - 1);
                break;
            }

            toReturn = new Token(TokenType.ILLEGAL, "", this.pos);
            break;
        }

        if(toReturn == null)
            toReturn = new Token(TokenType.EOF, "", this.pos);

        this.pos++;
        return toReturn;
    }

    private static boolean canStartIdentifier(int character) {
        return character == '_' || (character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z');
    }

    private static boolean isValidForIdentifier(int character) {
        return canStartIdentifier(character) || Character.isDigit(character);
    }
}
