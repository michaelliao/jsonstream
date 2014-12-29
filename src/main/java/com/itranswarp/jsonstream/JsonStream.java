package com.itranswarp.jsonstream;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse JSON as stream and return parsed object.
 * 
 * @author Michael Liao
 */
public class JsonStream {

    final TokenReader reader;
    final JsonObjectFactory jsonObjectFactory;
    final JsonArrayFactory jsonArrayFactory;
    final ObjectHook<?> objectHook;

    public JsonStream(Reader reader, JsonObjectFactory jsonObjectFactory,
            JsonArrayFactory jsonArrayFactory, ObjectHook<?> objectHook) {
        this.reader = new TokenReader(new CharReader(reader));
        this.jsonObjectFactory = jsonObjectFactory != null ? jsonObjectFactory
                : () -> {
                    return new HashMap<String, Object>();
                };
        this.jsonArrayFactory = jsonArrayFactory != null ? jsonArrayFactory
                : () -> {
                    return new ArrayList<Object>();
                };
        this.objectHook = objectHook != null ? objectHook : (map) -> {
            return map;
        };
    }

	boolean hasStatus(int expectedStatus) {
        return ((status & expectedStatus) > 0);
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

    Stack stack;
    int status;

    public Object parse() throws IOException {
        stack = new Stack();
        status = STATUS_EXPECT_SINGLE_VALUE | STATUS_EXPECT_BEGIN_OBJECT | STATUS_EXPECT_BEGIN_ARRAY;
        for (;;) {
            Token currentToken = reader.readNextToken();
            switch (currentToken) {
            case BOOLEAN:
                Boolean bool = reader.readBoolean();
                if (hasStatus(STATUS_EXPECT_SINGLE_VALUE)) {
                    // single boolean:
                    stack.push(StackValue.newJsonSingle(bool));
                    status = STATUS_EXPECT_END_DOCUMENT;
                    continue;
                }
                if (hasStatus(STATUS_EXPECT_OBJECT_VALUE)) {
                    String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                    stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, bool);
                    status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_OBJECT;
                    continue;
                }
                if (hasStatus(STATUS_EXPECT_ARRAY_VALUE)) {
                    stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(bool);
                    status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_ARRAY;
                    continue;
                }
                throw new RuntimeException("should not reach here.");

            case NULL:
                reader.readNull();
                if (hasStatus(STATUS_EXPECT_SINGLE_VALUE)) {
                    // single null:
                    stack.push(StackValue.newJsonSingle(null));
                    status = STATUS_EXPECT_END_DOCUMENT;
                    continue;
                }
                if (hasStatus(STATUS_EXPECT_OBJECT_VALUE)) {
                    String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                    stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, null);
                    status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_OBJECT;
                    continue;
                }
                if (hasStatus(STATUS_EXPECT_ARRAY_VALUE)) {
                    stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(null);
                    status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_ARRAY;
                    continue;
                }
                throw new RuntimeException("should not reach here.");

            case NUMBER:
                Number number = reader.readNumber();
                if (hasStatus(STATUS_EXPECT_SINGLE_VALUE)) {
                    // single number:
                    stack.push(StackValue.newJsonSingle(number));
                    status = STATUS_EXPECT_END_DOCUMENT;
                    continue;
                }
                if (hasStatus(STATUS_EXPECT_OBJECT_VALUE)) {
                    String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                    stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, number);
                    status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_OBJECT;
                    continue;
                }
                if (hasStatus(STATUS_EXPECT_ARRAY_VALUE)) {
                    stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(number);
                    status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_ARRAY;
                    continue;
                }
                throw new RuntimeException("should not reach here.");

            case STRING:
                String str = reader.readString();
                if (hasStatus(STATUS_EXPECT_SINGLE_VALUE)) {
                    // single string:
                    stack.push(StackValue.newJsonSingle(str));
                    status = STATUS_EXPECT_END_DOCUMENT;
                    continue;
                }
                if (hasStatus(STATUS_EXPECT_OBJECT_KEY)) {
                    stack.push(StackValue.newJsonObjectKey(str));
                    status = STATUS_EXPECT_COLON;
                    continue;
                }
                if (hasStatus(STATUS_EXPECT_OBJECT_VALUE)) {
                    String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                    stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, str);
                    status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_OBJECT;
                    continue;
                }
                if (hasStatus(STATUS_EXPECT_ARRAY_VALUE)) {
                    stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(str);
                    status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_ARRAY;
                    continue;
                }
                throw new RuntimeException("should not reach here.");

            case SEP_COLON: // :
                if (status == STATUS_EXPECT_COLON) {
                    status = STATUS_EXPECT_OBJECT_VALUE | STATUS_EXPECT_BEGIN_OBJECT | STATUS_EXPECT_BEGIN_ARRAY;
                    continue;
                }
                throw new JsonParseException("Unexpected char \':\'..", reader.reader.readed);

            case SEP_COMMA: // ,
                if (hasStatus(STATUS_EXPECT_COMMA)) {
                    if (hasStatus(STATUS_EXPECT_END_OBJECT)) {
                        status = STATUS_EXPECT_OBJECT_KEY;
                        continue;
                    }
                    if (hasStatus(STATUS_EXPECT_END_ARRAY)) {
                        status = STATUS_EXPECT_ARRAY_VALUE | STATUS_EXPECT_BEGIN_ARRAY | STATUS_EXPECT_BEGIN_OBJECT;
                        continue;
                    }
                }
                throw new JsonParseException("Unexpected char \',\'.", reader.reader.readed);

            case END_ARRAY:
                if (hasStatus(STATUS_EXPECT_END_ARRAY)) {
                    StackValue array = stack.pop(StackValue.TYPE_ARRAY);
                    if (stack.isEmpty()) {
                        stack.push(array);
                        status = STATUS_EXPECT_END_DOCUMENT;
                        continue;
                    }
                    int type = stack.getTopValueType();
                    if (type == StackValue.TYPE_OBJECT_KEY) {
                        // key: [ CURRENT ] ,}
                        String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                        stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, array.value);
                        status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_OBJECT;
                        continue;
                    }
                    if (type == StackValue.TYPE_ARRAY) {
                        // xx, xx, [CURRENT] ,]
                        stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(array.value);
                        status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_ARRAY;
                        continue;
                    }
                }
                throw new JsonParseException("Unexpected char: \']\'.", reader.reader.readed);

            case END_OBJECT:
                if (hasStatus(STATUS_EXPECT_END_OBJECT)) {
                    StackValue object = stack.pop(StackValue.TYPE_OBJECT);
                    if (stack.isEmpty()) {
                        stack.push(object);
                        status = STATUS_EXPECT_END_DOCUMENT;
                        continue;
                    }
                    int type = stack.getTopValueType();
                    if (type == StackValue.TYPE_OBJECT_KEY) {
                        String key = stack.pop(StackValue.TYPE_OBJECT_KEY).valueAsKey();
                        stack.peek(StackValue.TYPE_OBJECT).valueAsObject().put(key, object.value);
                        status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_OBJECT;
                        continue;
                    }
                    if (type == StackValue.TYPE_ARRAY) {
                        stack.peek(StackValue.TYPE_ARRAY).valueAsArray().add(object.value);
                        status = STATUS_EXPECT_COMMA | STATUS_EXPECT_END_ARRAY;
                        continue;
                    }
                }
                throw new JsonParseException("Unexpected char: \'}\'.", reader.reader.readed);

            case END_DOCUMENT:
                if (hasStatus(STATUS_EXPECT_END_DOCUMENT)) {
                    StackValue v = stack.pop();
                    if (stack.isEmpty()) {
                        if (v.type == StackValue.TYPE_OBJECT) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> map = (Map<String, Object>) v.value;
                            return this.objectHook.toObject(map);
                        }
                        return v.value;
                    }
                }
                throw new JsonParseException("Unexpected EOF.", reader.reader.readed);

            case BEGIN_ARRAY:
                if (hasStatus(STATUS_EXPECT_BEGIN_ARRAY)) {
                    stack.push(StackValue.newJsonArray(this.jsonArrayFactory.createJsonArray()));
                    status = STATUS_EXPECT_ARRAY_VALUE | STATUS_EXPECT_BEGIN_OBJECT | STATUS_EXPECT_BEGIN_ARRAY| STATUS_EXPECT_END_ARRAY;
                    continue;
                }
                throw new JsonParseException("Unexpected char: \'[\'.", reader.reader.readed);

            case BEGIN_OBJECT:
                if (hasStatus(STATUS_EXPECT_BEGIN_OBJECT)) {
                    stack.push(StackValue.newJsonObject(this.jsonObjectFactory.createJsonObject()));
                    status = STATUS_EXPECT_OBJECT_KEY | STATUS_EXPECT_BEGIN_OBJECT | STATUS_EXPECT_END_OBJECT;
                    continue;
                }
                throw new JsonParseException("Unexpected char: \'{\'.", reader.reader.readed);
        	}
        }
	}

    /**
     * Should read EOF for next token.
     */
    static final int STATUS_EXPECT_END_DOCUMENT   = 0x0002;

    /**
     * Should read "{" for next token.
     */
    static final int STATUS_EXPECT_BEGIN_OBJECT   = 0x0004;

    /**
     * Should read "}" for next token.
     */
    static final int STATUS_EXPECT_END_OBJECT     = 0x0008;

    /**
     * Should read object key for next token.
     */
    static final int STATUS_EXPECT_OBJECT_KEY     = 0x0010;

    /**
     * Should read object value for next token.
     */
    static final int STATUS_EXPECT_OBJECT_VALUE   = 0x0020;

    /**
     * Should read ":" for next token.
     */
    static final int STATUS_EXPECT_COLON          = 0x0040;

    /**
     * Should read "," for next token.
     */
    static final int STATUS_EXPECT_COMMA          = 0x0080;

    /**
     * Should read "[" for next token.
     */
    static final int STATUS_EXPECT_BEGIN_ARRAY    = 0x0100;

    /**
     * Should read "]" for next token.
     */
    static final int STATUS_EXPECT_END_ARRAY      = 0x0200;

    /**
     * Should read array value for next token.
     */
    static final int STATUS_EXPECT_ARRAY_VALUE    = 0x0400;

    /**
     * Should read a single value for next token (must not be "{" or "[").
     */
    static final int STATUS_EXPECT_SINGLE_VALUE   = 0x0800;

}
