package com.itranswarp.jsonstream.format;

import java.util.regex.Pattern;

/**
 * Validate a string contains only numeric characters (0-9).
 * 
 * @author Michael Liao
 */
public class Numeric implements StringFormat {

    Pattern pattern = Pattern.compile("^[0-9]+$");

    @Override
    public boolean validate(String value) {
        return pattern.matcher(value).matches();
    }

}
