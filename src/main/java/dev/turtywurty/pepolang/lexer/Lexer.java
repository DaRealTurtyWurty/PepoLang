package dev.turtywurty.pepolang.lexer;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

// TODO: Integrate source reader and ring buffer
// TODO: Support for unicode characters
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

            if (current == '/') {
                Optional<Token> divToken = readSlash();
                if (divToken.isEmpty())
                    continue;

                toReturn = divToken.get();
                break;
            }

            if (TokenType.SINGLE_CHAR_TOKENS.containsKey(current)) {
                toReturn = new Token(TokenType.SINGLE_CHAR_TOKENS.get(current), "", this.pos);
                break;
            }

            if (current == '"') {
                toReturn = readString();
                break;
            }

            if (current == '\'') {
                toReturn = readCharacter();
                break;
            }

            if(current == '`') {
                toReturn = readMultiLineString();
                break;
            }

            if (canStartIdentifier(current)) {
                toReturn = readIdentifier(current);
                break;
            }

            if (Character.isDigit(current)) {
                toReturn = readNumber(current);
                break;
            }

            toReturn = new Token(TokenType.ILLEGAL, "", this.pos);
            break;
        }

        if (toReturn == null)
            toReturn = new Token(TokenType.EOF, "", this.pos);

        this.pos++;
        System.out.println(toReturn);
        return toReturn;
    }

    private Optional<Token> readSlash() {
        if (this.pos + 1 >= this.src.length)
            return Optional.of(new Token(TokenType.DIV, "", this.pos));

        char nextChar = (char) this.src[this.pos + 1];
        if (nextChar == '/') { // reached a comment
            while (this.pos < this.src.length && this.src[this.pos] != '\n') {
                this.pos++;
            }

            this.pos++;
            return Optional.empty();
        }

        if (nextChar == '*') { // reached a multi-line comment
            boolean reachedCommentEnd = false;
            while (this.pos < this.src.length) {
                if (this.src[this.pos] == '*') { // maybe reached the end?
                    if (this.pos + 1 < this.src.length && this.src[this.pos + 1] == '/') { // check by looking for a */
                        this.pos++;
                        reachedCommentEnd = true;
                        break;
                    }
                }

                this.pos++;
            }

            if (reachedCommentEnd) {
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

            if (++this.pos >= this.src.length)
                break;

            currentChar = (char) this.src[this.pos];
        } while (isValidForIdentifier(currentChar));

        this.pos--;

        String identifierStr = identifier.toString();
        if (TokenType.KEYWORDS.containsKey(identifierStr)) {
            return new Token(TokenType.KEYWORDS.get(identifierStr), "", this.pos - 1);
        }

        return new Token(TokenType.IDENTIFIER, identifierStr, this.pos - 1);
    }

    private Token readString() {
        var str = new StringBuilder();
        this.pos++;

        while (this.pos < this.src.length) {
            char current = (char) this.src[this.pos];
            if (current == '"') {
                return new Token(TokenType.STRING, str.toString(), this.pos - 1);
            }

            if (current == '\n') {
                return new Token(TokenType.ILLEGAL, str.toString(), this.pos++);
            }

            if (current == '\\') {
                this.pos++;
                if (this.pos >= this.src.length) break;
                current = (char) this.src[this.pos];
                str.append(parseEscapeSequence(current));
            } else {
                str.append(current);
            }

            this.pos++;
        }

        return new Token(TokenType.ILLEGAL, str.toString(), this.pos);
    }

    private Token readMultiLineString() {
        var str = new StringBuilder();
        this.pos++;

        while (this.pos < this.src.length) {
            char current = (char) this.src[this.pos];
            if (current == '`')
                return new Token(TokenType.MULTI_LINE_STRING, str.toString(), this.pos - 1);

            if (current == '\\') {
                this.pos++;
                if (this.pos >= this.src.length) break;
                current = (char) this.src[this.pos];
                str.append(parseEscapeSequence(current));
            } else {
                str.append(current);
            }

            this.pos++;
        }

        return new Token(TokenType.ILLEGAL, str.toString(), this.pos);
    }

    private Token readCharacter() {
        this.pos++;

        if (this.pos >= this.src.length)
            return new Token(TokenType.ILLEGAL, "", this.pos);

        char current = (char) this.src[this.pos];
        if (current == '\\') {
            if (this.pos + 1 < this.src.length && this.src[this.pos + 1] == 'u') {
                this.pos++;

                var builder = new StringBuilder("\\u");
                while (++this.pos < this.src.length && builder.length() < 6) {
                    current = (char) this.src[this.pos];
                    if (isHexadecimal(current)) {
                        builder.append(current);
                    } else {
                        break;
                    }
                }

                if (builder.length() != 6)
                    return new Token(TokenType.ILLEGAL, builder.toString(), this.pos);

                char nextChar = (char) this.src[this.pos];
                if (nextChar != '\'')
                    return new Token(TokenType.ILLEGAL, builder.toString(), this.pos);

                try {
                    return new Token(TokenType.CHARACTER, new String(Character.toChars(Integer.parseInt(builder.substring(2), 16))), this.pos);
                } catch (NumberFormatException ignored) {
                    return new Token(TokenType.ILLEGAL, builder.toString(), this.pos);
                }
            } else {
                if (++this.pos >= this.src.length)
                    return new Token(TokenType.ILLEGAL, "", this.pos);

                current = (char) this.src[this.pos];
                Character escape = parseEscapeSequence(current);
                return new Token(
                        escape != null ? TokenType.CHARACTER : TokenType.ILLEGAL,
                        escape != null ? String.valueOf(escape) : "",
                        this.pos++);
            }
        } else {
            int byte1 = this.src[this.pos] & 0xFF;
            if ((byte1 & 0x80) != 0) { // Checks if the most significant bit is 1
                try {
                    int codePoint = decodeUtf8CodePoint();
                    while (this.src[this.pos] != '\'') {
                        this.pos++;
                        if(this.pos >= this.src.length)
                            return new Token(TokenType.ILLEGAL, "", --this.pos);
                    }

                    return new Token(TokenType.CHARACTER, new String(Character.toChars(codePoint)), this.pos);
                } catch (IllegalArgumentException ignored) {
                    while (this.src[this.pos] != '\'') {
                        this.pos++;
                        if(this.pos >= this.src.length)
                            return new Token(TokenType.ILLEGAL, "", --this.pos);
                    }

                    return new Token(TokenType.ILLEGAL, "", this.pos);
                }
            } else if (++this.pos < this.src.length && (char) this.src[this.pos] == '\'') {
                return new Token(TokenType.CHARACTER, String.valueOf(current), this.pos);
            }
        }

        return new Token(TokenType.ILLEGAL, String.valueOf(current), this.pos - 1);
    }

    private int decodeUtf8CodePoint() throws IllegalArgumentException {
        if (this.pos >= this.src.length)
            throw new IllegalArgumentException("End of input reached");

        int byte1 = this.src[this.pos] & 0xFF; // 0xFF is because bytes in java are signed, and we need unsigned (0-255)
        int codePoint;

        if((byte1 & 0x80) == 0) { // Checks the most significant bit is 0 (0xxxxxxx) - a single byte character
            codePoint = byte1;
        } else if((byte1 & 0xE0) == 0xC0) { // Checks the two most significant bits are 110xxxxx - a 2 byte character
            if (this.pos + 1 >= this.src.length)
                throw new IllegalArgumentException("End of input reached");

            int byte2 = this.src[++this.pos] & 0xFF;
            if((byte2 & 0xC0) != 0x80) // Checks the two most significant bits are 10xxxxxx
                throw new IllegalArgumentException("Invalid UTF-8 character");

            codePoint = ((byte1 & 0x1F) << 6) | (byte2 & 0x3F); // 1st byte contributes 5 bits, 2nd byte contributes 6 bits

            // Overlong encoding check: Two-byte sequences must represent code points >= 0x80
            if (codePoint < 0x80)
                throw new IllegalArgumentException("Overlong UTF-8 encoding");
        } else if ((byte1 & 0xF0) == 0xE0) { // Checks the four most significant bits are 1110xxxx - a 3 byte character
            if (this.pos + 2 >= this.src.length)
                throw new IllegalArgumentException("End of input reached");

            int byte2 = this.src[++this.pos] & 0xFF;
            int byte3 = this.src[++this.pos] & 0xFF;

            if((byte2 & 0xC0) != 0x80 || (byte3 & 0xC0) != 0x80)
                throw new IllegalArgumentException("Invalid UTF-8 character");

            codePoint = ((byte1 & 0x0F) << 12) | ((byte2 & 0x3F) << 6) | (byte3 & 0x3F); // 1st byte contributes 4 bits, 2nd byte contributes 6 bits, 3rd byte contributes 6 bits

            // Overlong encoding check: Three-byte sequences must represent code points >= 0x800
            if (codePoint < 0x800)
                throw new IllegalArgumentException("Overlong UTF-8 encoding");
        } else if((byte1 & 0xF8) == 0xF0) { // Checks the five most significant bits are 11110xxx - a 4 byte character
            if (this.pos + 3 >= this.src.length)
                throw new IllegalArgumentException("End of input reached");

            int byte2 = this.src[++this.pos] & 0xFF;
            int byte3 = this.src[++this.pos] & 0xFF;
            int byte4 = this.src[++this.pos] & 0xFF;
            if((byte2 & 0xC0) != 0x80 || (byte3 & 0xC0) != 0x80 || (byte4 & 0xC0) != 0x80)
                throw new IllegalArgumentException("Invalid UTF-8 character");

            codePoint = ((byte1 & 0x07) << 18) | ((byte2 & 0x3F) << 12) | ((byte3 & 0x3F) << 6) | (byte4 & 0x3F); // 1st byte contributes 3 bits, 2nd byte contributes 6 bits, 3rd byte contributes 6 bits, 4th byte contributes 6 bits

            // Overlong encoding check: Four-byte sequences must represent code points >= 0x10000
            if (codePoint < 0x10000 || codePoint > 0x10FFFF)
                throw new IllegalArgumentException("Overlong or out-of-range UTF-8 encoding");
        } else {
            throw new IllegalArgumentException("Invalid UTF-8 character");
        }

        this.pos++;
        return codePoint;
    }

    private Token readNumber(char currentChar) {
        TokenType type = TokenType.NUMBER_INT;
        var number = new StringBuilder();

        if(currentChar == '0' && this.pos + 1 < this.src.length) {
            number.append(currentChar);
            currentChar = (char) this.src[++this.pos];
            type = switch (Character.toLowerCase(currentChar)) {
                case 'x' -> TokenType.NUMBER_HEXADECIMAL;
                case 'b' -> TokenType.NUMBER_BINARY;
                case '0', '1', '2', '3', '4', '5', '6', '7' -> TokenType.NUMBER_OCTAL;
                case '.' -> TokenType.NUMBER_DOUBLE;
                default -> type;
            };

            if(type.isNonDecimalIntegralLiteral()) {
                number.append(currentChar);

                integralLiteralLoop: while (++this.pos < this.src.length) {
                    currentChar = (char) this.src[this.pos];

                    switch (type) {
                        case NUMBER_OCTAL -> {
                            if(isTerminatingCharacter(currentChar))
                                break integralLiteralLoop;

                            if(currentChar < '0' || currentChar > '7') {
                                type = TokenType.NUMBER_INT;
                                break integralLiteralLoop;
                            }
                        }
                        case NUMBER_HEXADECIMAL -> {
                            if(isTerminatingCharacter(currentChar))
                                break integralLiteralLoop;

                            if(!isHexadecimal(currentChar)) {
                                number.append(currentChar);

                                while (++this.pos < this.src.length) {
                                    currentChar = (char) this.src[this.pos];
                                    if(isTerminatingCharacter(currentChar))
                                        break;

                                    number.append(currentChar);
                                }

                                return new Token(TokenType.ILLEGAL, number.toString(), this.pos - 1);
                            }
                        }
                        case NUMBER_BINARY -> {
                            if(isTerminatingCharacter(currentChar))
                                break integralLiteralLoop;

                            if(currentChar != '0' && currentChar != '1') {
                                number.append(currentChar);

                                while (++this.pos < this.src.length) {
                                    currentChar = (char) this.src[this.pos];
                                    if(isTerminatingCharacter(currentChar))
                                        break;

                                    number.append(currentChar);
                                }

                                return new Token(TokenType.ILLEGAL, number.toString(), this.pos - 1);
                            }
                        }
                    }

                    number.append(currentChar);
                }

                if (type.isNonDecimalIntegralLiteral()) {
                    this.pos--;
                    return new Token(type, number.toString(), this.pos - 1);
                }
            }
        }

        do {
            if(isTerminatingCharacter(currentChar))
                break;

            if(currentChar == '_') {
                if(this.pos + 1 >= this.src.length)
                    return new Token(TokenType.ILLEGAL, number.toString(), this.pos);

                currentChar = (char) this.src[++this.pos];
                if(!Character.isDigit(currentChar))
                    return new Token(TokenType.ILLEGAL, number.toString(), this.pos);

                number.append(currentChar);
                continue;
            }

            number.append(currentChar);

            if(currentChar != '.' && !Character.isDigit(currentChar)) {
                type = TokenType.ILLEGAL;
                while (++this.pos < this.src.length) {
                    currentChar = (char) this.src[this.pos];
                    if(isTerminatingCharacter(currentChar))
                        break;

                    number.append(currentChar);
                }

                break;
            }

            if (++this.pos >= this.src.length)
                break;

            currentChar = (char) this.src[this.pos];

            if (currentChar == '.') {
                type = TokenType.NUMBER_DOUBLE;
                if (number.indexOf(".") != -1) {
                    type = TokenType.ILLEGAL;
                    number.append(currentChar);
                    while (++this.pos < this.src.length) {
                        currentChar = (char) this.src[this.pos];
                        if(isTerminatingCharacter(currentChar))
                            break;

                        number.append(currentChar);
                    }

                    break;
                }
            } else if (Character.toLowerCase(currentChar) == 'd') {
                type = TokenType.NUMBER_DOUBLE;
                number.append(currentChar);
                this.pos++;
                break;
            } else if (Character.toLowerCase(currentChar) == 'f') {
                type = TokenType.NUMBER_FLOAT;
                number.append(currentChar);
                this.pos++;
                break;
            } else if (Character.toLowerCase(currentChar) == 'l') {
                type = TokenType.NUMBER_LONG;
                number.append(currentChar);
                this.pos++;
                break;
            } else if (currentChar == '_') {
                if(this.pos + 1 >= this.src.length)
                    break;

                currentChar = (char) this.src[++this.pos];
                if(!Character.isDigit(currentChar))
                    break;

                number.append(currentChar);
            }
        } while (this.pos < this.src.length);

        if(this.pos < this.src.length && (type == TokenType.NUMBER_DOUBLE || type == TokenType.NUMBER_FLOAT || type == TokenType.NUMBER_LONG)) {
            currentChar = (char) this.src[this.pos];

            while (true) {
                if(isTerminatingCharacter(currentChar))
                    break;

                type = TokenType.ILLEGAL;
                number.append(currentChar);
                if(++this.pos >= this.src.length)
                    break;

                currentChar = (char) this.src[this.pos];
            }
        }

        this.pos--;
        return new Token(type, number.toString(), this.pos - 1);
    }

    private static boolean isHexadecimal(char character) {
        return Character.isDigit(character) || (character >= 'A' && character <= 'F') || (character >= 'a' && character <= 'f');
    }

    private static Character parseEscapeSequence(char escapeChar) {
        return switch (escapeChar) {
            case 'n' -> '\n';
            case 't' -> '\t';
            case 'r' -> '\r';
            case '0' -> '\0';
            case 'b' -> '\b';
            case '\\' -> '\\';
            case '"' -> '"';
            case '\'' -> '\'';
            default -> null;
        };
    }

    private static boolean canStartIdentifier(int character) {
        return character == '_' || (character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z');
    }

    private static boolean isValidForIdentifier(int character) {
        return canStartIdentifier(character) || Character.isDigit(character);
    }

    private static boolean isTerminatingCharacter(int character) {
        return Character.isWhitespace(character) || TokenType.SINGLE_CHAR_TOKENS.containsKey((char) character);
    }
}
