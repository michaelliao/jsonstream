package com.itranswarp.jsonstream;

/**
 * Exception when serialize to JSON.
 * 
 * @author Michael Liao
 */
public class JsonSerializeException extends RuntimeException {

    /**
     * Construct a JsonSerializeException.
     * 
     * @param message The exception message.
     */
    public JsonSerializeException(String message) {
        super(message);
    }
}
