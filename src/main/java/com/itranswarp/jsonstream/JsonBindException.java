package com.itranswarp.jsonstream;

/**
 * Exception when bind JSON to JavaBean.
 * 
 * @author Michael Liao
 */
public class JsonBindException extends RuntimeException {

    public JsonBindException(String message, Throwable t) {
        super(message, t);
    }

    public JsonBindException(String message) {
        super(message);
	}

}
