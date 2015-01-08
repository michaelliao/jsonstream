package com.itranswarp.jsonstream;

/**
 * Exception when processing with JSON and Java objects.
 * 
 * @author Michael Liao
 */
public class JsonException extends RuntimeException {

    public JsonException() {
    }

    public JsonException(String message) {
        super(message);
    }

    public JsonException(Throwable cause) {
        super(cause);
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

}
