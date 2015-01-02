package com.itranswarp.jsonstream;

/**
 * Exception when serialize to JSON.
 * 
 * @author Michael Liao
 */
public class JsonSerializeException extends RuntimeException {

    public JsonSerializeException(String message) {
        super(message);
    }
}
