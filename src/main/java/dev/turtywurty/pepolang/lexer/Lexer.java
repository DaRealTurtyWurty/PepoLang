package dev.turtywurty.pepolang.lexer;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

// TODO: Circular Buffer
// TODO: peek() and peek(k) methods
// TODO: consume() and consume(k) methods
// TODO: Support for multi-line strings
// TODO: Split the reader into a separate class that can take a string, byte[] or InputStream
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
                Optional<Token> divToken = readSlash();
                if(divToken.isEmpty())
                    continue;

                toReturn = divToken.get();
                break;
            }

            if(TokenType.SINGLE_CHAR_TOKENS.containsKey(current)) {
                toReturn = new Token(TokenType.SINGLE_CHAR_TOKENS.get(current), "", this.pos);
                break;
            }

            if(current == '"') {
                toReturn = readString();
                break;
            }

            if(canStartIdentifier(current)) {
                toReturn = readIdentifier(current);
                break;
            }

            if(Character.isDigit(current)) {
                toReturn = readNumber(current);
                break;
            }

            toReturn = new Token(TokenType.ILLEGAL, "", this.pos);
            break;
        }

        if(toReturn == null)
            toReturn = new Token(TokenType.EOF, "", this.pos);

        this.pos++;
        System.out.println(toReturn);
        return toReturn;
    }

    private Optional<Token> readSlash() {
        if(this.pos + 1 >= this.src.length)
            return Optional.of(new Token(TokenType.DIV, "", this.pos));

        char nextChar = (char) this.src[this.pos + 1];
        if(nextChar == '/') { // reached a comment
            while (this.pos < this.src.length && this.src[this.pos] != '\n') {
                this.pos++;
            }

            this.pos++;
            return Optional.empty();
        }

        if(nextChar == '*') { // reached a multi-line comment
            boolean reachedCommentEnd = false;
            while(this.pos < this.src.length) {
                if(this.src[this.pos] == '*') { // maybe reached the end?
                    if(this.pos + 1 < this.src.length && this.src[this.pos + 1] == '/') { // check by looking for a */
                        this.pos++;
                        reachedCommentEnd = true;
                        break;
                    }
                }

                this.pos++;
            }

            if(reachedCommentEnd) {
                this.pos++;
                return Optional.empty();
            } else {
                return Optional.of(new Token(TokenType.ILLEGAL, "", this.pos));
            }
        }

        return Optional.of(new Token(TokenType.DIV, "", this.pos));
    }

    private Token readIdentifier(char currentChar) {
        var identifier = new StringBuilder();
        do {
            identifier.append(currentChar);

            if(++this.pos >= this.src.length)
                break;

            currentChar = (char) this.src[this.pos];
        } while (isValidForIdentifier(currentChar));

        this.pos--;

        String identifierStr = identifier.toString();
        if(TokenType.KEYWORDS.containsKey(identifierStr)) {
            return new Token(TokenType.KEYWORDS.get(identifierStr), "", this.pos - 1);
        }

        return new Token(TokenType.IDENTIFIER, identifierStr, this.pos - 1);
    }

    private Token readString() {
        var str = new StringBuilder();
        this.pos++;

        while(this.pos < this.src.length) {
            char current = (char) this.src[this.pos];
            if(current == '"') {
                return new Token(TokenType.STRING, str.toString(), this.pos - 1);
            }

            if(current == '\n') {
                return new Token(TokenType.ILLEGAL, str.toString(), this.pos++);
            }

            if(current == '\\') {
                this.pos++;
                if(this.pos >= this.src.length) break;
                current = (char) this.src[this.pos];
                str.append(parseEscapeSequence(current));
            } else {
                str.append(current);
            }

            this.pos++;
        }

        return new Token(TokenType.ILLEGAL, str.toString(), this.pos);
    }

    private Token readNumber(char currentChar) {
        TokenType type = TokenType.NUMBER_INT;
        var number = new StringBuilder();
        do {
            number.append(currentChar);

            if(++this.pos >= this.src.length)
                break;

            currentChar = (char) this.src[this.pos];
            if(currentChar == '.') {
                type = TokenType.NUMBER_DOUBLE;
            }

            if(Character.toLowerCase(currentChar) == 'd') {
                type = TokenType.NUMBER_DOUBLE;
                number.append(currentChar);
                this.pos++;
                break;
            }

            if(Character.toLowerCase(currentChar) == 'f') {
                type = TokenType.NUMBER_FLOAT;
                number.append(currentChar);
                this.pos++;
                break;
            }
        } while (Character.isDigit(currentChar) || (currentChar == '.' && number.indexOf(".") == -1 && Character.isDigit((char) this.src[this.pos + 1])));

        this.pos--;
        return new Token(type, number.toString(), this.pos - 1);
    }

    private static char parseEscapeSequence(char escapeChar) {
        return switch (escapeChar) {
            case 'n' -> '\n';
            case 't' -> '\t';
            case 'r' -> '\r';
            case '0' -> '\0';
            case 'b' -> '\b';
            case '\\' -> '\\';
            case '"' -> '"';
            case '\'' -> '\'';
            default -> escapeChar;
        };
    }

    private static boolean canStartIdentifier(int character) {
        return character == '_' || (character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z');
    }

    private static boolean isValidForIdentifier(int character) {
        return canStartIdentifier(character) || Character.isDigit(character);
    }
}
