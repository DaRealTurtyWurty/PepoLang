package dev.turtywurty.pepolang.lexer;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

// TODO: Support for unicode characters
// TODO: Support for when double or triple characters are used for operators (e.g. ++, --, ==, !=, <=, >=, >>>, <<<, etc.)
public class Lexer {
    private final SourceReader reader;

    public Lexer(byte[] content) {
        this.reader = new SourceReader(content);
    }

    public Lexer(String str) {
        this(str.getBytes(StandardCharsets.UTF_8));
    }

    public Token nextToken() {
        Token toReturn = null;
        while (this.reader.hasNext()) {
            char current = this.reader.consume();
            if (Character.isWhitespace(current))
                continue;

            if (current == '/') {
                Optional<Token> divToken = readSlash();
                if (divToken.isEmpty())
                    continue;

                toReturn = divToken.get();
                break;
            }

            if (TokenType.SINGLE_CHAR_TOKENS.containsKey(current)) {
                toReturn = new Token(TokenType.SINGLE_CHAR_TOKENS.get(current), "", this.reader.getPos());
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

            toReturn = new Token(TokenType.ILLEGAL, "", this.reader.getPos());
            break;
        }

        if (toReturn == null)
            toReturn = new Token(TokenType.EOF, "", this.reader.getPos());

        System.out.println(toReturn);
        return toReturn;
    }

    private Optional<Token> readSlash() {
        if (!this.reader.hasNext())
            return Optional.of(new Token(TokenType.DIV, "", this.reader.getPos()));

        char nextChar = this.reader.peek();
        if (nextChar == '/') { // reached a comment
            while (this.reader.hasNext()) {
                if(this.reader.consume() == '\n')
                    break;
            }

            return Optional.empty();
        }

        if (nextChar == '*') { // reached a multi-line comment
            boolean reachedCommentEnd = false;
            while (this.reader.hasNext()) {
                if (this.reader.consume() == '*') { // maybe reached the end?
                    if (this.reader.hasNext() && this.reader.peek() == '/') { // check by looking for a */
                        this.reader.consume();
                        reachedCommentEnd = true;
                        break;
                    }
                }
            }

            if (reachedCommentEnd) {
                this.reader.consume();
                return Optional.empty();
            } else {
                return Optional.of(new Token(TokenType.ILLEGAL, "", this.reader.getPos()));
            }
        }

        return Optional.of(new Token(TokenType.DIV, "", this.reader.getPos()));
    }

    private Token readIdentifier(char currentChar) {
        var identifier = new StringBuilder();
        identifier.append(currentChar);

        while (this.reader.hasNext() && isValidForIdentifier(this.reader.peek())) {
            currentChar = this.reader.consume();
            identifier.append(currentChar);
        };

        String identifierStr = identifier.toString();
        if (TokenType.KEYWORDS.containsKey(identifierStr))
            return new Token(TokenType.KEYWORDS.get(identifierStr), "", this.reader.getPos());

        return new Token(TokenType.IDENTIFIER, identifierStr, this.reader.getPos());
    }

    private Token readString() {
        var str = new StringBuilder();

        while (this.reader.hasNext()) {
            char current = this.reader.consume();
            if (current == '"')
                return new Token(TokenType.STRING, str.toString(), this.reader.getPos() - 1);

            if (current == '\n') {
                while (this.reader.hasNext()) {
                    current = this.reader.consume();
                    if (current == '"')
                        return new Token(TokenType.ILLEGAL, str.toString(), this.reader.getPos());
                }

                return new Token(TokenType.ILLEGAL, str.toString(), this.reader.getPos());
            }

            if (current == '\\') {
                if (!this.reader.hasNext())
                    break;

                current = this.reader.consume();
                str.append(parseEscapeSequence(current));
            } else {
                str.append(current);
            }
        }

        return new Token(TokenType.ILLEGAL, str.toString(), this.reader.getPos());
    }

    private Token readMultiLineString() {
        var str = new StringBuilder();

        while (this.reader.hasNext()) {
            char current = this.reader.consume();
            if (current == '`')
                return new Token(TokenType.MULTI_LINE_STRING, str.toString(), this.reader.getPos() - 1);

            if (current == '\\') {
                if (!this.reader.hasNext())
                    break;

                current = this.reader.consume();
                str.append(parseEscapeSequence(current));
            } else {
                str.append(current);
            }
        }

        return new Token(TokenType.ILLEGAL, str.toString(), this.reader.getPos());
    }

    private Token readCharacter() {
        if (!this.reader.hasNext())
            return new Token(TokenType.ILLEGAL, "", this.reader.getPos());

        char current = this.reader.peek();
        if (current == '\\') {
            this.reader.consume();
            if (this.reader.hasNext() && this.reader.peek() == 'u') {
                this.reader.consume();
                var builder = new StringBuilder("\\u");
                while (this.reader.hasNext() && builder.length() < 6) {
                    current = this.reader.consume();
                    if (isHexadecimal(current)) {
                        builder.append(current);
                    } else {
                        break;
                    }
                }

                if (builder.length() != 6)
                    return new Token(TokenType.ILLEGAL, builder.toString(), this.reader.getPos());

                char nextChar = this.reader.consume();
                if (nextChar != '\'')
                    return new Token(TokenType.ILLEGAL, builder.toString(), this.reader.getPos());

                try {
                    return new Token(TokenType.CHARACTER,
                            new String(Character.toChars(Integer.parseInt(builder.substring(2), 16))),
                            this.reader.getPos());
                } catch (NumberFormatException ignored) {
                    return new Token(TokenType.ILLEGAL, builder.toString(), this.reader.getPos());
                }
            } else {
                if (!this.reader.hasNext())
                    return new Token(TokenType.ILLEGAL, "", this.reader.getPos());

                current = this.reader.consume();
                Character escape = parseEscapeSequence(current);
                boolean isNextCharValid = this.reader.hasNext() && this.reader.consume() == '\'';
                return new Token(
                        escape != null && isNextCharValid ? TokenType.CHARACTER : TokenType.ILLEGAL,
                        escape != null ? String.valueOf(escape) : "",
                        this.reader.getPos());
            }
        } else {
            int byte1 = this.reader.peekByte() & 0xFF;
            if ((byte1 & 0x80) != 0) { // Checks if the most significant bit is 1
                try {
                    int codePoint = decodeUtf8CodePoint();
                    while (this.reader.hasNext() && this.reader.consume() != '\'') {
                        if(!this.reader.hasNext())
                            return new Token(TokenType.ILLEGAL, "", this.reader.getPos());
                    }

                    return new Token(TokenType.CHARACTER, new String(Character.toChars(codePoint)), this.reader.getPos());
                } catch (IllegalArgumentException ignored) {
                    while (this.reader.hasNext() && this.reader.consume() != '\'') {
                        if(!this.reader.hasNext())
                            return new Token(TokenType.ILLEGAL, "", this.reader.getPos());
                    }

                    return new Token(TokenType.ILLEGAL, "", this.reader.getPos());
                }
            }

            current = this.reader.consume();
            if (this.reader.hasNext() && this.reader.consume() == '\'') {
                return new Token(TokenType.CHARACTER, String.valueOf(current), this.reader.getPos());
            }
        }

        return new Token(TokenType.ILLEGAL, String.valueOf(current), this.reader.getPos());
    }

    private int decodeUtf8CodePoint() throws IllegalArgumentException {
        if (!this.reader.hasNext())
            throw new IllegalArgumentException("End of input reached");

        int byte1 = this.reader.consumeByte() & 0xFF; // 0xFF is because bytes in java are signed, and we need unsigned (0-255)
        int codePoint;

        if((byte1 & 0x80) == 0) { // Checks the most significant bit is 0 (0xxxxxxx) - a single byte character
            codePoint = byte1;
        } else if((byte1 & 0xE0) == 0xC0) { // Checks the two most significant bits are 110xxxxx - a 2 byte character
            if (!this.reader.hasNext())
                throw new IllegalArgumentException("End of input reached");

            int byte2 = this.reader.consumeByte() & 0xFF;
            if((byte2 & 0xC0) != 0x80) // Checks the two most significant bits are 10xxxxxx
                throw new IllegalArgumentException("Invalid UTF-8 character");

            codePoint = ((byte1 & 0x1F) << 6) | (byte2 & 0x3F); // 1st byte contributes 5 bits, 2nd byte contributes 6 bits

            // Overlong encoding check: Two-byte sequences must represent code points >= 0x80
            if (codePoint < 0x80)
                throw new IllegalArgumentException("Overlong UTF-8 encoding");
        } else if ((byte1 & 0xF0) == 0xE0) { // Checks the four most significant bits are 1110xxxx - a 3 byte character
            if (!this.reader.hasNext(2))
                throw new IllegalArgumentException("End of input reached");

            int byte2 = this.reader.consumeByte() & 0xFF;
            int byte3 = this.reader.consumeByte() & 0xFF;

            if((byte2 & 0xC0) != 0x80 || (byte3 & 0xC0) != 0x80)
                throw new IllegalArgumentException("Invalid UTF-8 character");

            codePoint = ((byte1 & 0x0F) << 12) | ((byte2 & 0x3F) << 6) | (byte3 & 0x3F); // 1st byte contributes 4 bits, 2nd byte contributes 6 bits, 3rd byte contributes 6 bits

            // Overlong encoding check: Three-byte sequences must represent code points >= 0x800
            if (codePoint < 0x800)
                throw new IllegalArgumentException("Overlong UTF-8 encoding");
        } else if((byte1 & 0xF8) == 0xF0) { // Checks the five most significant bits are 11110xxx - a 4 byte character
            if (!this.reader.hasNext(3))
                throw new IllegalArgumentException("End of input reached");

            int byte2 = this.reader.consumeByte() & 0xFF;
            int byte3 = this.reader.consumeByte() & 0xFF;
            int byte4 = this.reader.consumeByte() & 0xFF;
            if((byte2 & 0xC0) != 0x80 || (byte3 & 0xC0) != 0x80 || (byte4 & 0xC0) != 0x80)
                throw new IllegalArgumentException("Invalid UTF-8 character");

            codePoint = ((byte1 & 0x07) << 18) | ((byte2 & 0x3F) << 12) | ((byte3 & 0x3F) << 6) | (byte4 & 0x3F); // 1st byte contributes 3 bits, 2nd byte contributes 6 bits, 3rd byte contributes 6 bits, 4th byte contributes 6 bits

            // Overlong encoding check: Four-byte sequences must represent code points >= 0x10000
            if (codePoint < 0x10000 || codePoint > 0x10FFFF)
                throw new IllegalArgumentException("Overlong or out-of-range UTF-8 encoding");
        } else {
            throw new IllegalArgumentException("Invalid UTF-8 character");
        }

        return codePoint;
    }

    private Token readNumber(char currentChar) {
        TokenType type = TokenType.NUMBER_INT;
        var number = new StringBuilder();

        if(currentChar == '0' && this.reader.hasNext()) {
            number.append(currentChar);
            currentChar = this.reader.peek();
            type = switch (Character.toLowerCase(currentChar)) {
                case 'x' -> TokenType.NUMBER_HEXADECIMAL;
                case 'b' -> TokenType.NUMBER_BINARY;
                case '0', '1', '2', '3', '4', '5', '6', '7' -> TokenType.NUMBER_OCTAL;
                case '.' -> TokenType.NUMBER_DOUBLE;
                default -> type;
            };

            if(type.isNonDecimalIntegralLiteral()) {
                currentChar = this.reader.consume();
                number.append(currentChar);

                integralLiteralLoop: while (this.reader.hasNext()) {
                    currentChar = this.reader.peek();

                    if (isTerminatingCharacter(currentChar))
                        break;

                    currentChar = this.reader.consume();

                    switch (type) {
                        case NUMBER_OCTAL -> {
                            if(currentChar < '0' || currentChar > '7') {
                                type = TokenType.NUMBER_INT;
                                break integralLiteralLoop;
                            }
                        }
                        case NUMBER_HEXADECIMAL -> {
                            if(!isHexadecimal(currentChar)) {
                                number.append(currentChar);

                                while (this.reader.hasNext() && !isTerminatingCharacter(this.reader.peek())) {
                                    currentChar = this.reader.consume();
                                    number.append(currentChar);
                                }

                                return new Token(TokenType.ILLEGAL, number.toString(), this.reader.getPos());
                            }
                        }
                        case NUMBER_BINARY -> {
                            if(currentChar != '0' && currentChar != '1') {
                                number.append(currentChar);

                                while (this.reader.hasNext() && !isTerminatingCharacter(this.reader.peek())) {
                                    currentChar = this.reader.consume();
                                    number.append(currentChar);
                                }

                                return new Token(TokenType.ILLEGAL, number.toString(), this.reader.getPos());
                            }
                        }
                    }

                    number.append(currentChar);
                }

                if (type.isNonDecimalIntegralLiteral())
                    return new Token(type, number.toString(), this.reader.getPos() - 1);
            }
        }

        if(isTerminatingCharacter(currentChar))
            return new Token(TokenType.NUMBER_INT, number.toString(), this.reader.getPos() - 1);

        if(currentChar == '_') {
            if(!this.reader.hasNext())
                return new Token(TokenType.ILLEGAL, number.toString(), this.reader.getPos());

            currentChar = this.reader.consume();
            if(!Character.isDigit(currentChar))
                return new Token(TokenType.ILLEGAL, number.toString(), this.reader.getPos());

            number.append(currentChar);
        } else {
            number.append(currentChar);
        }

        if(currentChar != '.' && !Character.isDigit(currentChar)) {
            type = TokenType.ILLEGAL;
            while (this.reader.hasNext()) {
                currentChar = this.reader.consume();
                if(isTerminatingCharacter(currentChar))
                    break;

                number.append(currentChar);
            }

            return new Token(type, number.toString(), this.reader.getPos() - 1);
        }

        while (reader.hasNext() && !isTerminatingCharacter(this.reader.peek())) {
            currentChar = this.reader.consume();

            while (currentChar == '_') {
                if (!this.reader.hasNext())
                    break;

                currentChar = this.reader.consume();
                if(!Character.isDigit(currentChar)) {
                    number.append(currentChar);
                    while (this.reader.hasNext()) {
                        currentChar = this.reader.peek();
                        if(!isTerminatingCharacter(currentChar))
                            currentChar = this.reader.consume();
                    }
                }
            }

            number.append(currentChar);

            if (currentChar == '.') {
                type = TokenType.NUMBER_DOUBLE;
                if (countOccurrences(number, '.') > 1) {
                    type = TokenType.ILLEGAL;
                    while (this.reader.hasNext()) {
                        currentChar = this.reader.consume();
                        if(isTerminatingCharacter(currentChar))
                            break;

                        number.append(currentChar);
                    }

                    break;
                }
            } else if (Character.toLowerCase(currentChar) == 'd') {
                type = TokenType.NUMBER_DOUBLE;
                break;
            } else if (Character.toLowerCase(currentChar) == 'f') {
                type = TokenType.NUMBER_FLOAT;
                break;
            } else if (Character.toLowerCase(currentChar) == 'l') {
                type = TokenType.NUMBER_LONG;
                break;
            } else if (!Character.isDigit(currentChar)) {
                type = TokenType.ILLEGAL;
                while (this.reader.hasNext() && !isTerminatingCharacter(this.reader.peek())) {
                    currentChar = this.reader.consume();
                    number.append(currentChar);
                }

                break;
            }
        };

        if(this.reader.hasNext() && (type == TokenType.NUMBER_DOUBLE || type == TokenType.NUMBER_FLOAT || type == TokenType.NUMBER_LONG)) {
            while (this.reader.hasNext() && !isTerminatingCharacter(this.reader.peek())) {
                currentChar = this.reader.consume();
                type = TokenType.ILLEGAL;
                number.append(currentChar);
            }
        }

        return new Token(type, number.toString(), this.reader.getPos() - 1);
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

    private static int countOccurrences(CharSequence string, char toCount) {
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == toCount)
                count++;
        }

        return count;
    }
}
