package com.itranswarp.jsonstream.format;

/**
 * Validate a string is all upper cased.
 * 
 * @author Michael Liao
 */
public class UpperCase implements StringFormat {

    @Override
    public boolean validate(String value) {
        return value.toUpperCase().equals(value);
    }

}
