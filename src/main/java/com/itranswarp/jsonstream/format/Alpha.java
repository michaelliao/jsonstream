package com.itranswarp.jsonstream.format;

import java.util.regex.Pattern;

/**
 * Validate a string contains only alpha characters (a-z, A-Z).
 * 
 * @author Michael Liao
 */
public class Alpha implements StringFormat {

    Pattern pattern = Pattern.compile("^[a-zA-Z]+$");

    @Override
    public boolean validate(String value) {
        return pattern.matcher(value).matches();
    }

}
