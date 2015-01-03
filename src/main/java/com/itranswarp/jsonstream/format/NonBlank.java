package com.itranswarp.jsonstream.format;

/**
 * Validate a string is not blank.
 * 
 * @author Michael Liao
 */
public class NonBlank implements StringFormat {

    @Override
    public boolean validate(String value) {
        return value.trim().length() == value.length();
    }

}
