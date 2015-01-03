package com.itranswarp.jsonstream.format;

/**
 * A StringFormat is a way to validate String value by code, used combined with 
 * {@code Format.CLASS} annotation.
 * 
 * @author Michael Liao
 */
public interface StringFormat {

    /**
     * Validate the string value, return true if it is ok, otherwise false.
     * 
     * @param value The String value.
     * @return True if validate ok.
     */
    boolean validate(String value);

}
