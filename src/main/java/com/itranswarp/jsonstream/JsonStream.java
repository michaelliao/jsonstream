package com.itranswarp.jsonstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonStream {

    final TokenReader reader;

	public JsonStream(Reader reader) {
		this.reader = new TokenReader(new CharReader(reader));
	}

	public JsonStream(String str) {
		this(new StringReader(str));
	}

	public JsonStream(InputStream input) throws IOException {
		this(new InputStreamReader(input, "UTF-8"));
	}

	<T> T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

    Stack stack;
    boolean isSingleValue;
    Token lastToken;
    int status;

    /**
     * Should read EOF for next token.
     */
    static final int STATUS_READ_END_DOCUMENT   = 0x0002;

    /**
     * Should read "{" for next token.
     */
    static final int STATUS_READ_BEGIN_OBJECT   = 0x0004;

    /**
     * Should read "}" for next token.
     */
    static final int STATUS_READ_END_OBJECT     = 0x0008;

    /**
     * Should read object key for next token.
     */
    static final int STATUS_READ_OBJECT_KEY     = 0x0010;

    /**
     * Should read object value for next token.
     */
    static final int STATUS_READ_OBJECT_VALUE   = 0x0020;

    /**
     * Should read ":" for next token.
     */
    static final int STATUS_READ_COLON          = 0x0040;

    /**
     * Should read "," for next token.
     */
    static final int STATUS_READ_COMMA          = 0x0080;

    /**
     * Should read "[" for next token.
     */
    static final int STATUS_READ_BEGIN_ARRAY    = 0x0100;

    /**
     * Should read "]" for next token.
     */
    static final int STATUS_READ_END_ARRAY      = 0x0200;

    /**
     * Should read array value for next token.
     */
    static final int STATUS_READ_ARRAY_VALUE    = 0x0400;

    /**
     * Should read a single value for next token (excludes "{" and "[").
     */
    static final int STATUS_READ_SINGLE_VALUE   = 0x0800;

    boolean hasStatus(int expectedStatus) {
        return ((status & expectedStatus) > 0);
    }

    Map<String, Object> newObject() {
        return new HashMap<String, Object>();
    }

    List<Object> newArray() {
        return new ArrayList<Object>();
    }

    Object checkExpectedType(Object obj, Class<?> clazz) {
        if (clazz.isInstance(obj)) {
            return obj;
        }
        if (obj==null && clazz==Object.class) {
            return null;
        }
        throw new ClassCastException("Cannot case parsed result to expected type: " + clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> T parse(Class<T> clazz) throws IOException {
        return (T) checkExpectedType(parse(), clazz);
    }

    public Object parse() throws IOException {
        stack = new Stack();
        status = STATUS_READ_SINGLE_VALUE | STATUS_READ_BEGIN_OBJECT | STATUS_READ_BEGIN_ARRAY;
        for (;;) {
        	Token currentToken = reader.readNextToken();
        	switch (currentToken) {
            case BOOLEAN:
                Boolean bool = reader.readBoolean();
                if (hasStatus(STATUS_READ_SINGLE_VALUE)) {
                    // single boolean:
                    stack.push(StackValue.newJsonSingle(bool));
                    status = STATUS_READ_END_DOCUMENT;
                    continue;
                }
                if (status == STATUS_READ_OBJECT_VALUE) {
                    String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                    stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, bool);
                    status = STATUS_READ_COMMA | STATUS_READ_END_OBJECT;
                    continue;
                }
                if (status == STATUS_READ_ARRAY_VALUE) {
                    stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(bool);
                    status = STATUS_READ_COMMA | STATUS_READ_END_ARRAY;
                    continue;
                }
                throw new RuntimeException("should not reach here.");

            case NULL:
                reader.readNull();
                if (hasStatus(STATUS_READ_SINGLE_VALUE)) {
                    // single null:
                    stack.push(StackValue.newJsonSingle(null));
                    status = STATUS_READ_END_DOCUMENT;
                    continue;
                }
                if (status == STATUS_READ_OBJECT_VALUE) {
                    String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                    stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, null);
                    status = STATUS_READ_COMMA | STATUS_READ_END_OBJECT;
                    continue;
                }
                if (status == STATUS_READ_ARRAY_VALUE) {
                    stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(null);
                    status = STATUS_READ_COMMA | STATUS_READ_END_ARRAY;
                    continue;
                }
                throw new RuntimeException("should not reach here.");

            case NUMBER:
                Number number = reader.readNumber();
                if (hasStatus(STATUS_READ_SINGLE_VALUE)) {
                    // single number:
                    stack.push(StackValue.newJsonSingle(number));
                    status = STATUS_READ_END_DOCUMENT;
                    continue;
                }
                if (status == STATUS_READ_OBJECT_VALUE) {
                    String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                    stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, number);
                    status = STATUS_READ_COMMA | STATUS_READ_END_OBJECT;
                    continue;
                }
                if (status == STATUS_READ_ARRAY_VALUE) {
                    stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(number);
                    status = STATUS_READ_COMMA | STATUS_READ_END_ARRAY;
                    continue;
                }
                throw new RuntimeException("should not reach here.");

            case STRING:
                String str = reader.readString();
                if (hasStatus(STATUS_READ_SINGLE_VALUE)) {
                    // single string:
                    stack.push(StackValue.newJsonSingle(str));
                    status = STATUS_READ_END_DOCUMENT;
                    continue;
                }
                if (hasStatus(STATUS_READ_OBJECT_KEY)) {
                    stack.push(StackValue.newJsonObjectKey(str));
                    status = STATUS_READ_COLON;
                    continue;
                }
                if (status == STATUS_READ_OBJECT_VALUE) {
                    String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                    stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, str);
                    status = STATUS_READ_COMMA | STATUS_READ_END_OBJECT;
                    continue;
                }
                if (hasStatus(STATUS_READ_ARRAY_VALUE)) {
                    stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(str);
                    status = STATUS_READ_COMMA | STATUS_READ_END_ARRAY;
                    continue;
                }
                throw new RuntimeException("should not reach here.");

            case COLON_SEPERATOR: // :
                if (status == STATUS_READ_COLON) {
                    status = STATUS_READ_OBJECT_VALUE;
                    continue;
                }
                throw new JsonParseException("Unexpected char \':\'..", reader.reader.readed);

            case COMMA_SEPERATOR: // ,
                if (hasStatus(STATUS_READ_COMMA)) {
                    if (hasStatus(STATUS_READ_END_OBJECT)) {
                        status = STATUS_READ_OBJECT_KEY;
                        continue;
                    }
                    if (hasStatus(STATUS_READ_END_ARRAY)) {
                        status = STATUS_READ_ARRAY_VALUE;
                        continue;
                    }
                }
                throw new JsonParseException("Unexpected char \',\'.", reader.reader.readed);

            case END_ARRAY:
                if (hasStatus(STATUS_READ_END_ARRAY)) {
                    StackValue array = stack.pop(StackValue.TYPE_ARRAY);
                    if (stack.isEmpty()) {
                        stack.push(array);
                        status = STATUS_READ_END_DOCUMENT;
                        continue;
                    }
                    int type = stack.getTopValueType();
                    if (type == StackValue.TYPE_OBJECT_KEY) {
                        // key: [ CURRENT ] ,}
                        String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                        stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, array.value);
                        status = STATUS_READ_COMMA | STATUS_READ_END_OBJECT;
                        continue;
                    }
                    if (type == StackValue.TYPE_ARRAY) {
                        // xx, xx, [CURRENT] ,]
                        stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(array.value);
                        status = STATUS_READ_COMMA | STATUS_READ_END_ARRAY;
                        continue;
                    }
                }
                throw new JsonParseException("Unexpected char: \']\'.", reader.reader.readed);

            case END_OBJECT:
                if (hasStatus(STATUS_READ_END_OBJECT)) {
                    StackValue object = stack.pop(StackValue.TYPE_OBJECT);
                    if (stack.isEmpty()) {
                        stack.push(object);
                        status = STATUS_READ_END_DOCUMENT;
                        continue;
                    }
                    int type = stack.getTopValueType();
                    if (type == StackValue.TYPE_OBJECT_KEY) {
                        String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                        stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, object.value);
                        status = STATUS_READ_COMMA | STATUS_READ_END_OBJECT;
                        continue;
                    }
                    if (type == StackValue.TYPE_ARRAY) {
                        stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(object.value);
                        status = STATUS_READ_COMMA | STATUS_READ_END_ARRAY;
                        continue;
                    }
                }
                throw new JsonParseException("Unexpected char: \'}\'.", reader.reader.readed);

            case END_DOCUMENT:
                if (hasStatus(STATUS_READ_END_DOCUMENT)) {
                    StackValue v = stack.pop();
                    if (stack.isEmpty()) {
                        return v.value;
                    }
                }
                throw new JsonParseException("Unexpected EOF.", reader.reader.readed);

            case START_ARRAY:
                if (hasStatus(STATUS_READ_BEGIN_ARRAY)) {
                    stack.push(StackValue.newJsonArray(newArray()));
                    status = STATUS_READ_ARRAY_VALUE | STATUS_READ_END_ARRAY;
                    continue;
                }
                throw new JsonParseException("Unexpected char: \'[\'.", reader.reader.readed);

            case START_OBJECT:
                if (hasStatus(STATUS_READ_BEGIN_OBJECT)) {
                    stack.push(StackValue.newJsonObject(newObject()));
                    status = STATUS_READ_OBJECT_KEY | STATUS_READ_END_OBJECT;
                    continue;
                }
                throw new JsonParseException("Unexpected char: \'{\'.", reader.reader.readed);
        	}
        }
	}

}
