package com.itranswarp.jsonstream;

/**
 * Exception when processing with JSON and Java objects.
 * 
 * @author Michael Liao
 */
public class JsonException extends RuntimeException {

    /**
     * Default JsonException.
     */
    public JsonException() {
    }

    /**
     * JsonException with message.
     * 
     * @param message The exception message.
     */
    public JsonException(String message) {
        super(message);
    }

    /**
     * JsonException with throwable.
     * 
     * @param cause The Throwable object.
     */
    public JsonException(Throwable cause) {
        super(cause);
    }

    /**
     * JsonException with message and throwable.
     * 
     * @param message The exception message.
     * @param cause The Throwable object.
     */
    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

}
