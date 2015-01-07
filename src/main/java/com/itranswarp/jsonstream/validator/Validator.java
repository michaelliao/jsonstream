package com.itranswarp.jsonstream.validator;

/**
 * Validate property values.
 * 
 * @author Michael Liao
 */
public interface Validator<T> {

    /**
     * Validate a property value. Throws ValidateException If validation failed.
     * 
     * @param propValue The property value.
     * @param path The path of the JSON document. e.g. "Group.Users[0].Address".
     * @param name The property name of the JSON document. e.g. "zipcode".
     */
	void validate(T propValue, String path, String name);
}
