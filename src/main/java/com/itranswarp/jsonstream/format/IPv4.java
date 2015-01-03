package com.itranswarp.jsonstream.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate an IPv4 address like "127.0.0.1".
 * 
 * @author Michael Liao
 */
public class IPv4 implements StringFormat {

    Pattern pattern = Pattern.compile("^(\\d?\\d?\\d)\\.(\\d?\\d?\\d)\\.(\\d?\\d?\\d)\\.(\\d?\\d?\\d)$");

    @Override
    public boolean validate(String value) {
        Matcher m = pattern.matcher(value);
        if (m.matches()) {
            for (int i=1; i<=4; i++) {
                if (Integer.parseInt(m.group(i)) > 255) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
