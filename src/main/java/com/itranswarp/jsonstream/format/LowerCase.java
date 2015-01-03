package com.itranswarp.jsonstream.format;

/**
 * Validate a string is all lower cased.
 * 
 * @author Michael Liao
 */
public class LowerCase implements StringFormat {

    @Override
    public boolean validate(String value) {
        return value.toLowerCase().equals(value);
    }

}
