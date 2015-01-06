package com.itranswarp.jsonstream;

/**
 * Exception when bind JSON to JavaBean.
 * 
 * @author Michael Liao
 */
public class JsonBindException extends RuntimeException {

    /**
     * Create JsonBindException with message and nested exception.
     * 
     * @param message The message.
     * @param t The nested exception.
     */
    public JsonBindException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * Create JsonBindException with message.
     * 
     * @param message The message.
     */
    public JsonBindException(String message) {
        super(message);
	}

}
