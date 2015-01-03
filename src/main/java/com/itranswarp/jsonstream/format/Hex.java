package com.itranswarp.jsonstream.format;

import java.util.regex.Pattern;

/**
 * Validate a string contains only hex characters (a-f, A-F, 0-9).
 * 
 * @author Michael Liao
 */
public class Hex implements StringFormat {

    Pattern pattern = Pattern.compile("^[a-fA-F0-9]+$");

    @Override
    public boolean validate(String value) {
        return pattern.matcher(value).matches();
    }

}
