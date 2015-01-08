package com.itranswarp.jsonstream.validator.impl;

import java.lang.reflect.AnnotatedElement;

import com.itranswarp.jsonstream.JsonValidateException;
import com.itranswarp.jsonstream.annotation.Required;
import com.itranswarp.jsonstream.validator.Validator;

/**
 * A BooleanValidator.
 * 
 * @author Michael Liao
 */
public class BooleanValidator implements Validator<Boolean> {

    final boolean required;

    public BooleanValidator(AnnotatedElement ae) {
        required = ae.isAnnotationPresent(Required.class);
    }

    public void validate(Boolean obj, String path, String name) {
        if (required && obj == null) {
            throw new JsonValidateException("Required", path + "." + name);
        }
	}
}
