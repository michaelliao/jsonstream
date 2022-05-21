package com.itranswarp.jsonstream;

import java.io.IOException;

/**
 * Make a character stream as a Token stream.
 * 
 * @author Michael Liao
 */
class TokenReader {

    CharReader reader;

    TokenReader(CharReader reader) {
        this.reader = reader;
    }

    boolean isWhiteSpace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
    }

    Token readNextToken() throws IOException {
        char ch = '?';
        for (;;) {
            if (!reader.hasMore()) {
                // EOF:
                return Token.END_DOCUMENT;
            }
            ch = reader.peek();
            if (!isWhiteSpace(ch)) {
                break;
            }
            reader.next(); // skip white space
        }
        switch (ch) {
        case '{':
            reader.next(); // skip
            return Token.BEGIN_OBJECT;
        case '}':
            reader.next(); // skip
            return Token.END_OBJECT;
        case '[':
            reader.next(); // skip
            return Token.BEGIN_ARRAY;
        case ']':
            reader.next(); // skip
            return Token.END_ARRAY;
        case ':':
            reader.next(); // skip
            return Token.SEP_COLON;
        case ',':
            reader.next(); // skip
            return Token.SEP_COMMA;
        case '\"':
            return Token.STRING;
        case 'n':
            return Token.NULL;
        // true / false
        case 't':
        case 'f':
            return Token.BOOLEAN;
        case '-':
            return Token.NUMBER;
        default:
            if (ch >= '0' && ch <= '9') {
                return Token.NUMBER;
            }
            throw new JsonParseException("Parse error when try to guess next token.", reader.readed);
        }
    }

    // read string like "a encoded \u0098 \" str"
    String readString() throws IOException {
        StringBuilder sb = new StringBuilder(50);
        // first char must be ":
        char ch = reader.next();
        if (ch != '\"') {
            throw new JsonParseException("Expected \" but actual is: " + ch, reader.readed);
        }
        for (;;) {
            ch = reader.next();
            if (ch == '\\') {
                // escape: \" \\ \/ \b \f \n \r \t
                char ech = reader.next();
                switch (ech) {
                case '\"':
                    sb.append('\"');
                    break;
                case '\\':
                    sb.append('\\');
                    break;
                case '/':
                    sb.append('/');
                    break;
                case 'b':
                    sb.append('\b');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'u':
                    // read an unicode uXXXX:
                    int u = 0;
                    for (int i = 0; i < 4; i++) {
                        char uch = reader.next();
                        if (uch >= '0' && uch <= '9') {
                            u = (u << 4) + (uch - '0');
                        } else if (uch >= 'a' && uch <= 'f') {
                            u = (u << 4) + (uch - 'a') + 10;
                        } else if (uch >= 'A' && uch <= 'F') {
                            u = (u << 4) + (uch - 'A') + 10;
                        } else {
                            throw new JsonParseException("Unexpected char: " + uch, reader.readed);
                        }
                    }
                    sb.append((char) u);
                    break;
                default:
                    throw new JsonParseException("Unexpected char: " + ch, reader.readed);
                }
            } else if (ch == '\"') {
                // end of string:
                break;
            } else if (ch == '\r' || ch == '\n') {
                throw new JsonParseException("Unexpected char: " + ch, reader.readed);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    // read a true or false:
    boolean readBoolean() throws IOException {
        char ch = reader.next();
        String expected = null;
        if (ch == 't') {
            expected = "rue"; // true
        } else if (ch == 'f') {
            expected = "alse"; // false
        } else {
            throw new JsonParseException("Unexpected char: " + ch, reader.readed);
        }
        for (int i = 0; i < expected.length(); i++) {
            char theChar = reader.next();
            if (theChar != expected.charAt(i)) {
                throw new JsonParseException("Unexpected char: " + theChar, reader.readed);
            }
        }
        return ch == 't';
    }

    // read a null:
    void readNull() throws IOException {
        String expected = "null";
        for (int i = 0; i < expected.length(); i++) {
            char theChar = reader.next();
            if (theChar != expected.charAt(i)) {
                throw new JsonParseException("Unexpected char: " + theChar, reader.readed);
            }
        }
    }

    static final int READ_NUMBER_INT_PART = 0;
    static final int READ_NUMBER_FRA_PART = 1;
    static final int READ_NUMBER_EXP_PART = 2;
    static final int READ_NUMBER_END = 3;

    // read number and return Long or Double:
    Number readNumber() throws IOException {
        StringBuilder intPart = null; // ###.xxxExxx
        StringBuilder fraPart = null; // xxx.###Exxx
        StringBuilder expPart = null; // xxx.xxxE###
        boolean hasFraPart = false;
        boolean hasExpPart = false;
        char ch = reader.peek();
        boolean minusSign = ch == '-';
        boolean expMinusSign = false;
        if (minusSign) {
            reader.next();
        }
        int status = READ_NUMBER_INT_PART;
        for (;;) {
            if (reader.hasMore()) {
                ch = reader.peek();
            } else {
                status = READ_NUMBER_END;
            }
            switch (status) {
            case READ_NUMBER_INT_PART:
                if (ch >= '0' && ch <= '9') {
                    if (intPart == null) {
                        intPart = new StringBuilder(10);
                    }
                    intPart.append(reader.next());
                } else if (ch == '.') {
                    if (intPart == null) {
                        throw new JsonParseException("Unexpected char: " + ch, reader.readed);
                    }
                    reader.next();
                    hasFraPart = true;
                    status = READ_NUMBER_FRA_PART;
                } else if (ch == 'e' || ch == 'E') {
                    reader.next();
                    hasExpPart = true;
                    // try to determin exp part's sign:
                    char signChar = reader.peek();
                    if (signChar == '-' || signChar == '+') {
                        expMinusSign = signChar == '-';
                        reader.next();
                    }
                    status = READ_NUMBER_EXP_PART;
                } else {
                    if (intPart == null) {
                        throw new JsonParseException("Unexpected char: " + reader.next(), reader.readed);
                    }
                    // end of number:
                    status = READ_NUMBER_END;
                }
                continue;
            case READ_NUMBER_FRA_PART:
                if (ch >= '0' && ch <= '9') {
                    if (fraPart == null) {
                        fraPart = new StringBuilder(10);
                    }
                    fraPart.append(reader.next());
                } else if (ch == 'e' || ch == 'E') {
                    reader.next();
                    hasExpPart = true;
                    // try to determin exp part's sign:
                    char signChar = reader.peek();
                    if (signChar == '-' || signChar == '+') {
                        expMinusSign = signChar == '-';
                        reader.next();
                    }
                    status = READ_NUMBER_EXP_PART;
                } else {
                    if (fraPart == null) {
                        throw new JsonParseException("Unexpected char: " + reader.next(), reader.readed);
                    }
                    // end of number:
                    status = READ_NUMBER_END;
                }
                continue;
            case READ_NUMBER_EXP_PART:
                if (ch >= '0' && ch <= '9') {
                    if (expPart == null) {
                        expPart = new StringBuilder(10);
                    }
                    expPart.append(reader.next());
                } else {
                    if (expPart == null) {
                        throw new JsonParseException("Unexpected char: " + reader.next(), reader.readed);
                    }
                    // end of number:
                    status = READ_NUMBER_END;
                }
                continue;
            case READ_NUMBER_END:
                // build parsed number:
                int readed = reader.readed;
                if (intPart == null) {
                    throw new JsonParseException("Missing integer part of number.", readed);
                }
                long lInt = minusSign ? -string2Long(intPart, readed) : string2Long(intPart, readed);
                if (!hasFraPart && !hasExpPart) {
                    return Long.valueOf(lInt);
                }
                if (hasFraPart && fraPart == null) {
                    throw new JsonParseException("Missing fraction part of number.", readed);
                }
                double dFraPart = hasFraPart ? (minusSign ? -string2Fraction(fraPart, readed) : string2Fraction(fraPart, readed)) : 0.0;
                double number = hasExpPart ? (lInt + dFraPart) * Math.pow(10, expMinusSign ? -string2Long(expPart, readed) : string2Long(expPart, readed))
                        : (lInt + dFraPart);
                if (number > MAX_SAFE_DOUBLE) {
                    throw new NumberFormatException("Exceeded maximum value: 1.7976931348623157e+308");
                }
                return Double.valueOf(number);
            default:
                continue;
            }
        }
    }

    static final long MAX_SAFE_INTEGER = 9007199254740991L;

    // parse "0123" as 123:
    long string2Long(CharSequence cs, int readed) {
        if (cs.length() > 16) {
            throw new JsonParseException("Number string is too long.", readed);
        }
        long n = 0;
        for (int i = 0; i < cs.length(); i++) {
            n = n * 10 + (cs.charAt(i) - '0');
            if (n > MAX_SAFE_INTEGER) {
                throw new JsonParseException("Exceeded maximum value: " + MAX_SAFE_INTEGER, readed);
            }
        }
        return n;
    }

    static final double MAX_SAFE_DOUBLE = 1.7976931348623157e+308;

    // parse "0123" as 0.0123
    double string2Fraction(CharSequence cs, int readed) {
        if (cs.length() > 16) {
            throw new JsonParseException("Number string is too long.", readed);
        }
        double d = 0.0;
        for (int i = 0; i < cs.length(); i++) {
            int n = cs.charAt(i) - '0';
            d = d + (n == 0 ? 0 : n / Math.pow(10, i + 1));
        }
        return d;
    }
}
