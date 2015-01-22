package com.itranswarp.jsonstream;

/**
 * Exception when parse invalid JSON or encountered an IOException.
 * 
 * @author Michael Liao
 */
public class JsonParseException extends JsonException {

    private final int errorIndex;

    /**
     * Parse exception with message and char index.
     * 
     * @param message The exception message.
     * @param errorIndex Char index.
     */
    public JsonParseException(String message, int errorIndex) {
        super(message);
        this.errorIndex = errorIndex;
    }

    /**
     * Parse exception with message.
     * 
     * @param message The exception message.
     */
    public JsonParseException(String message) {
    	this(message, -1);
	}

    /**
     * Get char index of current position when error occurred.
     * 
     * @return The error index.
     */
	public int getErrorIndex() {
        return this.errorIndex;
    }
}
