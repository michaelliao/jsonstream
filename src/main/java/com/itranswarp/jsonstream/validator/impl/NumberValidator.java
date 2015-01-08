package com.itranswarp.jsonstream.validator.impl;

import java.lang.reflect.AnnotatedElement;

import com.itranswarp.jsonstream.JsonValidateException;
import com.itranswarp.jsonstream.annotation.MaximumNumber;
import com.itranswarp.jsonstream.annotation.MinimumNumber;
import com.itranswarp.jsonstream.annotation.Required;
import com.itranswarp.jsonstream.validator.Validator;

/**
 * A NumberValidator accepts the following annotations:
 * 
 * <ul>
 *   <li>@Required</li>
 *   <li>@MinimumNumber</li>
 *   <li>@MaximumNumber</li>
 * </ul>
 * 
 * @author Michael Liao
 */
public class NumberValidator implements Validator<Double> {

	final boolean required;
	final Double minimum;
	final Double maximum;

	public NumberValidator(AnnotatedElement ae) {
		required = ae.isAnnotationPresent(Required.class);
		minimum = ae.isAnnotationPresent(MinimumNumber.class)
		        ? ae.getAnnotation(MinimumNumber.class).value() : null;
		maximum = ae.isAnnotationPresent(MaximumNumber.class)
		        ? ae.getAnnotation(MaximumNumber.class).value() : null;
	}

	public void validate(Double obj, String path, String name) {
		String fullpath = path + "." + name;
		if (required && obj == null) {
			throw new JsonValidateException("Required", fullpath);
		}
		if (obj == null) {
			return;
		}
		double value = obj.doubleValue();
		if ((minimum != null) && (value < minimum)) {
			throw new JsonValidateException("Minimum", fullpath);
		}
		if ((maximum != null) && (value > maximum)) {
			throw new JsonValidateException("Maximum", fullpath);
		}
	}
}
