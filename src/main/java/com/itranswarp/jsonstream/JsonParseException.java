package com.itranswarp.jsonstream;

/**
 * Exception when parse invalid JSON or encountered an IOException.
 * 
 * @author Michael Liao
 */
public class JsonParseException extends JsonException {

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
