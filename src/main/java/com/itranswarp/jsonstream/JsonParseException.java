package com.itranswarp.jsonstream;

/**
 * Exception when parse JSON.
 * 
 * @author Michael Liao
 */
public class JsonParseException extends RuntimeException {

    private final int errorIndex;

    public JsonParseException(String message, int errorIndex) {
        super(message);
        this.errorIndex = errorIndex;
    }

    public JsonParseException(String message) {
    	this(message, -1);
	}

	public int getErrorIndex() {
        return this.errorIndex;
    }
}
